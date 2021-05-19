package it.eng.edicolaservice.security;

import java.io.Serializable;

public class Token implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3999787844067815391L;
	String token;

	public void setToken(String token) {
		this.token = token;
	};

	public String getToken() {
		return this.token;
	};
}