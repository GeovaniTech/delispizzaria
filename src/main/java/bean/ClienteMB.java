package bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;

import controller.Connection;
import controller.Menssagens;
import model.Cliente;

@SuppressWarnings("deprecation")
@ManagedBean(name = "ClienteMB")
public class ClienteMB {
	private String nome;
	private String email;
	private int id;
	private Menssagens msg = new Menssagens();
	private static ArrayList<Cliente> clientes = new ArrayList<Cliente>();

	public ClienteMB() {
		atualizarClientes();
	}
	
	Connection cursor = new Connection();
	
	public void addCliente() throws SQLException {
		
		ResultSet rs = cursor.executarBusca("SELECT MAX(id) FROM clientes;");
		
		while(rs.next()) {
			this.setId(rs.getInt("max") + 1);
		}
		
		if (!nome.isEmpty() && !email.isEmpty()) {
			String comando_sql = "INSERT INTO clientes (id, nome, email) VALUES (" + this.getId() + ", '"
					+ this.getNome() + "', '" + this.getEmail() + "')";

			cursor.executarSQL(comando_sql);
			
			limparCampos();
			atualizarClientes();
			msg.cadastroCompleto();
		} else {
			msg.errorCamposNulos();
		}
	}

	public void atualizarClientes() {
		
		ResultSet resultado = cursor.executarBusca("SELECT * FROM clientes");
		
		if (resultado != null) {
			try {
				clientes.clear();
				
				while(resultado.next()) {				
					Cliente cliente = new Cliente();
					
					cliente.setId(resultado.getInt("id"));
					cliente.setNome(resultado.getString("nome"));
					cliente.setEmail(resultado.getString("email"));
					
					clientes.add(cliente);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void removerCliente(int id) {
		String comando_sql = "DELETE FROM clientes WHERE id = " + id;
		cursor.executarSQL(comando_sql);
		
		atualizarClientes();
	}
	
	public String limparCampos() {
		this.setNome(null);
		this.setEmail(null);
		return null;
	}
	
	//Getters e Setters
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<Cliente> getClientes() {
		return clientes;
	}

	public static void setClientes(ArrayList<Cliente> clientes) {
		ClienteMB.clientes = clientes;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
