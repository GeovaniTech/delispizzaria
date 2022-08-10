package controller;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Connection {
	private String url;
	private String user;
	private String password;
	private java.sql.Connection con;
	
	public Connection() {
		url = "jdbc:postgresql://localhost:5432/delispizzaria";
		user = "postgres";
		password = "SUA SENHA";
		
		try {
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int executarSQL(String sql) {
		try {
			Statement stm = con.createStatement();
			int res = stm.executeUpdate(sql);
			
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}	
	}
	
	public ResultSet executarBusca(String sql) {
		try {
			Statement stm = con.createStatement();
			ResultSet resultadoBusca = stm.executeQuery(sql);
			return resultadoBusca;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
