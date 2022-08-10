package bean;


import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import controller.Connection;
import controller.Menssagens;
import model.Cliente;
import model.Pizza;
import model.Sabor;

@SuppressWarnings("deprecation")
@ManagedBean(name = "PizzaProntaMB")
@SessionScoped
public class PizzaProntaMB {
	private int id; // Novo id, ao cadastrar uma pizza
	private int id_pizza_pedido; // Id da pizza no carrinho
	private int id_pizza; //Id da pizza já cadastrada, para ir ao carrinho
	private String sabor;
	private Sabor sabor_convertido;
	private Cliente cliente;
	private int tamanho;
	private static ArrayList<Pizza> pizzas = new ArrayList<Pizza>();
	
	Connection cursor = new Connection();
	Menssagens msg = new Menssagens();
	
	public PizzaProntaMB() {
		atualizarPizzas();
	}
	
	//Pizzas - Pedido
	public void addPizzaPedido() {
		idPizzaPedido();
		
		ResultSet rs = cursor.executarBusca("SELECT * FROM pizzas_prontas WHERE id = " + this.getId_pizza());
		
		SaborMB sa = new SaborMB();
		
		try {
			while(rs.next()) {
				
				int sabor = rs.getInt("sabor_id");
				
				for (int i = 0; i < sa.getList_sabor().size(); i++) {
					if (sa.getList_sabor().get(i).getId() == sabor) {
						this.setSabor_convertido(sa.getList_sabor().get(i));
						break;
					}
				}
				cursor.executarSQL("INSERT INTO pizzas (id, id_cliente, sabor, tamanho, confirmado) VALUES ("
					+ this.getId_pizza_pedido() + ", " + this.getCliente().getId() + ", '" + this.getSabor_convertido().getSabor()
					+ "', " + rs.getInt("tamanho") + ", " + false + ")");
			}
			
			PedidoMB pedido = new PedidoMB();
			
			PedidoMB.setCliente(this.getCliente());
			
			pedido.atualizarPizzas();
			pedido.atualizarCarrinho();
			redirecionarPara("carrinho.xhtml");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void idPizzaPedido() {
		ResultSet rs = cursor.executarBusca("SELECT MAX(id) FROM pizzas WHERE id_cliente = " + this.getCliente().getId());
		
		try {
			while(rs.next()) {
				this.setId_pizza_pedido(rs.getInt("max") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addIdPizzapedido(int id) {
		this.setId_pizza(id);
		redirecionarPara("selecionar_clientes.xhtml");
	}
	
	public void addCliente(Cliente cliente) {
		this.setCliente(cliente);
		addPizzaPedido();
	}
	
	//Pizzas - Banco
	public void addPizzaBanco() {
		idPizzaBanco();
		
		SaborMB sa = new SaborMB();
		
		for (int i = 0; i < sa.getList_sabor().size(); i++) {
			if (sa.getList_sabor().get(i).getSabor().equals(this.getSabor())) {
				this.setSabor_convertido(sa.getList_sabor().get(i));
				break;
			}
		}
		
		cursor.executarSQL("INSERT INTO pizzas_prontas (id, sabor_id, tamanho) VALUES ("
				+ this.getId() + ", " + this.getSabor_convertido().getId() + "," + this.getTamanho()
				+ ")");
		atualizarPizzas();
		msg.showMenssagem(FacesMessage.SEVERITY_INFO, "Pizza Cadastrada!", "Nova pizza adicionada");
	}
	
	public void removerPizza(int id) {
		cursor.executarSQL("DELETE FROM pizzas_prontas WHERE id = " + id);
		atualizarPizzas();
	}
	
	public void atualizarPizzas() {
		pizzas.clear();
		ResultSet rs = cursor.executarBusca("SELECT * FROM pizzas_prontas");
		
		SaborMB sa = new SaborMB();
		
		try {
			while(rs.next()) {
				Pizza pizza = new Pizza();
				
				pizza.setId(rs.getInt("id"));
				
				int sabor = rs.getInt("sabor_id");
				
				for (int i = 0; i < sa.getList_sabor().size(); i++) {
					if (sa.getList_sabor().get(i).getId() == sabor) {
						this.setSabor_convertido(sa.getList_sabor().get(i));
						break;
					}
				}
				
				pizza.setSabor(this.getSabor_convertido());
				pizza.setTamanho(rs.getInt("tamanho"));
				
				if(rs.getInt("tamanho") == 25) {
					pizza.setPreco(39.99);
				} else if (rs.getInt("tamanho") == 35) {
					pizza.setPreco(49.99);
				} else {
					pizza.setPreco(99.99);
				}
				pizzas.add(pizza);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void mostrarSabor() {
		System.out.println(this.getSabor());
		System.out.println(this.getTamanho());
	}
	
	public void idPizzaBanco() {
		ResultSet rs = cursor.executarBusca("SELECT MAX(id) FROM pizzas_prontas");
		try {
			while(rs.next()) {
				this.setId(rs.getInt("max") + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Funções auxiliares
	public void cancelar() {
		redirecionarPara("pedido.xhtml");
	}
	
	public void redirecionarPara(String url) {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		try {
			ec.redirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void atualizarPagina() {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		try {
			ec.redirect("/pizzaria" + ec.getRequestServletPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Getters e Setters
	public String getSabor() {
		return sabor;
	}
	public int getId_pizza_pedido() {
		return id_pizza_pedido;
	}

	public void setId_pizza_pedido(int id_pizza_pedido) {
		this.id_pizza_pedido = id_pizza_pedido;
	}

	public int getId_pizza() {
		return id_pizza;
	}

	public void setId_pizza(int id_pizza) {
		this.id_pizza = id_pizza;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	public Sabor getSabor_convertido() {
		return sabor_convertido;
	}
	public void setSabor_convertido(Sabor sabor_convertido) {
		this.sabor_convertido = sabor_convertido;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setSabor(String sabor) {
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
		PizzaProntaMB.pizzas = pizzas;
	}
}
