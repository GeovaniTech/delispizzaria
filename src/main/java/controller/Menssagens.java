package controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Menssagens {
	public void showMenssagem(FacesMessage.Severity severity, String titulo, String descricao) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, titulo, descricao));
	}
	
	public void errorCamposNulos() {
		showMenssagem(FacesMessage.SEVERITY_ERROR, "Campos Inválidos", "Há valores nulos.");
	}
	
	public void cadastroCompleto() {
		showMenssagem(FacesMessage.SEVERITY_INFO, "Cadastro Concluído", "Cadastro feito com sucesso");
	}
	
	public void errorClienteExistente() {
		showMenssagem(FacesMessage.SEVERITY_FATAL, "Cliente já cadastrado", "Já há um cliente com o id informado");
	}
}
