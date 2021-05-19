package it.eng.edicolaservice.security;

class Tenant{
	String issuer;
	String jwks_uri;
	
	public Tenant(String issuer, String jwks_uri) {
		this.issuer = issuer;
		this.jwks_uri = jwks_uri;
	};
	
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getIssuer() {
		return this.issuer;
	}
	public void setJwks_uri(String juri) {
		this.jwks_uri = juri;
	}
	public String getJwks_uri() {
		return this.jwks_uri;
	}				
}