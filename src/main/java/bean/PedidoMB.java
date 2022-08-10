package bean;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import controller.Connection;
import controller.Menssagens;
import model.Cliente;
import model.Pedido;
import model.Pizza;
import model.Sabor;

@SuppressWarnings("deprecation")
@ManagedBean(name = "PedidoMB")
@SessionScoped
public class PedidoMB {
	// Atributos pizza
	private int id_pizza;
	private Sabor sabor;
	private int tamanho;

	// Atributos pedido
	private static Cliente cliente;
	private static double valor_total;
	private int id_pedido;
	private Date data;

	// Visibilidade
	private boolean tamanho_rendered = true;
	private boolean sabor_rendered;
	private boolean cliente_rendered;
	private boolean confirmacao_rendered;
	private static boolean carrinho_empty;
	private static boolean carrinho_not_empty;

	// Etapa ativa
	private int stepIndex;

	Connection cursor = new Connection();
	Menssagens msg = new Menssagens();

	private static ArrayList<Pizza> pizzas = new ArrayList<Pizza>();
	private static ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
	
	public PedidoMB() {
		atualizarPedidos();
	}
	
	// Funções Pedido
	public void addPedido() {
		idPedido();
		dataPedido();

		String comando_sql = "INSERT INTO pedidos (id, data, total, cliente) VALUES (" + this.getId_pedido() + ", '"
				+ this.getData() + "', " + this.getValor_total() + ", " + this.getCliente().getId() + ")";
		cursor.executarSQL(comando_sql);

		cursor.executarSQL(
				"UPDATE pizzas SET confirmado =  " + true + " WHERE id_cliente = " + this.getCliente().getId());

		atualizarPedidos();
		atualizarPizzas();

		this.setTamanho_rendered(true);
		this.setCliente_rendered(false);
		this.setSabor_rendered(false);
		this.setConfirmacao_rendered(false);
		this.setStepIndex(0);

		redirecionarPara("/pizzaria/pages/listar-pedidos.xhtml");
	}

	public void atualizarPedidos() {
		ResultSet rs = cursor.executarBusca("SELECT * FROM pedidos ORDER BY data ASC");
		pedidos.clear();

		ClienteMB cliente = new ClienteMB();

		try {
			while (rs.next()) {
				Pedido pedido = new Pedido();

				int id_cliente = rs.getInt("cliente");

				for (int i = 0; i < cliente.getClientes().size(); i++) {
					if (id_cliente == cliente.getClientes().get(i).getId()) {
						pedido.setCliente(cliente.getClientes().get(i));
						break;
					}
				}
				pedido.setData(rs.getDate("data"));
				pedido.setId(rs.getInt("id"));
				pedido.setTotal(rs.getDouble("total"));
				pedidos.add(pedido);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void idPedido() {
		ResultSet rs = cursor.executarBusca("SELECT MAX(id) FROM pedidos");

		try {
			while (rs.next()) {
				this.setId_pedido(rs.getInt("max") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dataPedido() {
		Date data_atual = new Date();
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

		String data = formatador.format(data_atual);

		try {
			this.setData(formatador.parse(data));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void finalizarPedido(int id_pedido) {
		cursor.executarSQL("DELETE FROM pedidos WHERE id =" + id_pedido);
		atualizarPedidos();
		atualizarPizzas();
		msg.showMenssagem(FacesMessage.SEVERITY_INFO, "Pedido Concluído!", "Pedido finalizado com sucessso!");
	}

	// Funções Pizzas
	public void addTamanho(int tam) {
		this.setTamanho(tam);
		this.setTamanho_rendered(false);
		this.setSabor_rendered(true);
		this.setStepIndex(1);
		atualizarPagina();
	}

	public void addSabor(Sabor sabor) {
		this.setSabor(sabor);
		this.setSabor_rendered(false);
		this.setCliente_rendered(true);
		this.setStepIndex(2);
		atualizarPagina();
	}

	public void addCliente(Cliente cliente) {
		PedidoMB.setCliente(cliente);
		addPizza();
	}

	public void addPizza() {
		idPizza();

		String comando_sql = "INSERT INTO pizzas (id, id_cliente, sabor, tamanho, confirmado) VALUES ("
				+ this.getId_pizza() + ", " + this.getCliente().getId() + ", '" + this.getSabor().getSabor() + "', "
				+ this.getTamanho() + ", " + false + ")";

		cursor.executarSQL(comando_sql);
		msg.showMenssagem(FacesMessage.SEVERITY_INFO, "Pizza Adicionada", "Pizza adicionada com sucesso");

		this.setCliente_rendered(false);
		this.setConfirmacao_rendered(true);
		this.setStepIndex(3);

		atualizarPizzas();
		atualizarPagina();
	}

	public void novaPizza() {
		this.setTamanho_rendered(true);
		this.setCliente_rendered(false);
		this.setSabor_rendered(false);
		this.setConfirmacao_rendered(false);
		this.setStepIndex(0);

		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		try {
			ec.redirect("/pizzaria/pages/pedido.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removerPizza(int id) {
		cursor.executarSQL("DELETE FROM pizzas WHERE id = " + id + " AND id_cliente = " + this.getCliente().getId());
		atualizarPizzas();

		if (pizzas.size() == 0) {
			resetarTelas();
			PedidoMB.setCarrinho_empty(true);
			PedidoMB.setCarrinho_not_empty(false);
		} else {
			PedidoMB.setCarrinho_empty(false);
			PedidoMB.setCarrinho_not_empty(true);
			atualizarPagina();
		}
	}

	public void atualizarPizzas() {
		pizzas.clear();
		this.setValor_total(0);

		ResultSet rs = cursor.executarBusca(
				"SELECT * FROM pizzas WHERE id_cliente = " + this.getCliente().getId() + " AND confirmado = " + false);
		SaborMB sa = new SaborMB();

		try {
			while (rs.next()) {
				Pizza pizza = new Pizza();

				pizza.setId(rs.getInt("id"));
				pizza.setId_cliente(rs.getInt("id_cliente"));

				String sabor = rs.getString("sabor");

				for (int i = 0; i < sa.getList_sabor().size(); i++) {
					if (sabor.equals(sa.getList_sabor().get(i).getSabor())) {
						pizza.setSabor(sa.getList_sabor().get(i));
						break;
					}
				}
				pizza.setTamanho(rs.getInt("tamanho"));

				if (rs.getInt("tamanho") == 25) {
					this.setValor_total(this.getValor_total() + 39.99);
				}

				else if (rs.getInt("tamanho") == 35) {
					this.setValor_total(this.getValor_total() + 49.99);
				}

				else {
					this.setValor_total(this.getValor_total() + 99.99);
				}
				pizzas.add(pizza);
			}
			atualizarCarrinho();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void idPizza() {
		ResultSet rs = cursor
				.executarBusca("SELECT MAX(id) FROM pizzas WHERE id_cliente = " + this.getCliente().getId());

		try {
			while (rs.next()) {
				this.setId_pizza(rs.getInt("max") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Funções Auxiliares
	public void voltarStep() {
		this.setStepIndex(this.getStepIndex() - 1);

		if (this.getStepIndex() == 0) {
			this.setSabor_rendered(false);
			this.setTamanho_rendered(true);
		}

		else if (this.getStepIndex() == 1) {
			this.setCliente_rendered(false);
			this.setSabor_rendered(true);
		}

		else if (this.getStepIndex() == 2) {
			this.setConfirmacao_rendered(false);
			this.setCliente_rendered(true);
		}

		atualizarPagina();
	}

	public void atualizarPagina() {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		try {
			ec.redirect("/pizzaria" + ec.getRequestServletPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void redirecionarPara(String url) {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		try {
			ec.redirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void resetarTelas() {
		this.setTamanho_rendered(true);
		this.setCliente_rendered(false);
		this.setSabor_rendered(false);
		this.setConfirmacao_rendered(false);
		this.setStepIndex(0);
		atualizarPagina();
	}

	public void atualizarCarrinho() {
		if (pizzas.size() == 0) {
			PedidoMB.setCarrinho_empty(true);
			PedidoMB.setCarrinho_not_empty(false);
		} else {
			PedidoMB.setCarrinho_empty(false);
			PedidoMB.setCarrinho_not_empty(true);
		}
	}

	// Getters and Setters
	public int getId_pizza() {
		return id_pizza;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public static void setCliente(Cliente cliente) {
		PedidoMB.cliente = cliente;
	}

	public int getId_pedido() {
		return id_pedido;
	}

	public void setId_pedido(int id_pedido) {
		this.id_pedido = id_pedido;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public double getValor_total() {
		return valor_total;
	}

	public void setValor_total(double valor_total) {
		PedidoMB.valor_total = valor_total;
	}

	public void setId_pizza(int id_pizza) {
		this.id_pizza = id_pizza;
	}

	public Sabor getSabor() {
		return sabor;
	}

	public void setSabor(Sabor sabor) {
		this.sabor = sabor;
	}

	public int getTamanho() {
		return tamanho;
	}

	public void setTamanho(int tamanho) {
		this.tamanho = tamanho;
	}

	public ArrayList<Pizza> getPizzas() {
		return pizzas;
	}

	public static void setPizzas(ArrayList<Pizza> pizzas) {
		PedidoMB.pizzas = pizzas;
	}

	public ArrayList<Pedido> getPedidos() {
		return pedidos;
	}

	public static void setPedidos(ArrayList<Pedido> pedidos) {
		PedidoMB.pedidos = pedidos;
	}

	public boolean isTamanho_rendered() {
		return tamanho_rendered;
	}

	public void setTamanho_rendered(boolean tamanho_rendered) {
		this.tamanho_rendered = tamanho_rendered;
	}

	public boolean isSabor_rendered() {
		return sabor_rendered;
	}

	public void setSabor_rendered(boolean sabor_rendered) {
		this.sabor_rendered = sabor_rendered;
	}

	public boolean isCliente_rendered() {
		return cliente_rendered;
	}

	public void setCliente_rendered(boolean cliente_rendered) {
		this.cliente_rendered = cliente_rendered;
	}

	public boolean isConfirmacao_rendered() {
		return confirmacao_rendered;
	}

	public void setConfirmacao_rendered(boolean confirmacao_rendered) {
		this.confirmacao_rendered = confirmacao_rendered;
	}

	public boolean isCarrinho_empty() {
		return carrinho_empty;
	}

	public static void setCarrinho_empty(boolean carrinho_empty) {
		PedidoMB.carrinho_empty = carrinho_empty;
	}

	public boolean isCarrinho_not_empty() {
		return carrinho_not_empty;
	}

	public static void setCarrinho_not_empty(boolean carrinho_not_empty) {
		PedidoMB.carrinho_not_empty = carrinho_not_empty;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}
}
