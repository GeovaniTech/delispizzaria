package bean;

import java.sql.ResultSet;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;

import controller.Connection;
import controller.Menssagens;
import model.Sabor;

@SuppressWarnings("deprecation")
@ManagedBean(name = "SaborMB")
public class SaborMB {
	private String sabor;
	private String descricao;
	private Menssagens msg = new Menssagens();
	private static ArrayList<Sabor> list_sabor = new ArrayList<Sabor>();
	private int id;

	Connection cursor = new Connection();
	
	public SaborMB() {
		atualizarSabores();
	}	
	
	public void addSabor() {
		if (!sabor.isEmpty() && !descricao.isEmpty()) {
			
			ResultSet rs = cursor.executarBusca("SELECT MAX(id) FROM sabores");
			
			try {
				while(rs.next()) {
					this.setId(rs.getInt("max") + 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String comando_sql = "INSERT INTO sabores (id, sabor, descricao) VALUES (" + this.getId() + ", '"
					+ this.getSabor() + "', '" + this.getDescricao() + "')";
			
			cursor.executarSQL(comando_sql);
			
			limparCampos();
			atualizarSabores();
			msg.cadastroCompleto();
			
		} else {
			msg.errorCamposNulos();
		}
	}
	
	public void removerSabor(int id) {
		cursor.executarSQL("DELETE FROM sabores WHERE id = " + id);
		atualizarSabores();
	}
	
	public void atualizarSabores() {
		ResultSet resultado = cursor.executarBusca("SELECT * FROM sabores");
		
		if (resultado != null) {
			try {
				list_sabor.clear();
				while (resultado.next()) {
					Sabor sabor = new Sabor();
					
					sabor.setId(resultado.getInt("id"));
					sabor.setSabor(resultado.getString("sabor"));
					sabor.setDescricao(resultado.getString("descricao"));
					
					list_sabor.add(sabor);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String limparCampos() {
		this.setSabor(null);
		this.setDescricao(null);
		return null;
	}
	
	public ArrayList<Sabor> getList_sabor() {
		return list_sabor;
	}
	public void setList_sabor(ArrayList<Sabor> list_sabor) {
		SaborMB.list_sabor = list_sabor;
	}
	public String getSabor() {
		return sabor;
	}
	public void setSabor(String sabor) {
		this.sabor = sabor;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}	
}
