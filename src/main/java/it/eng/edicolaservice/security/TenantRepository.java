package it.eng.edicolaservice.security;


import org.springframework.stereotype.Repository;

@Repository
public class TenantRepository {

		
	
	public Tenant findById(String tenant) {
		Tenant t = new Tenant("https://accounts.google.com", "https://www.googleapis.com/oauth2/v3/certs");
		return t;
	}
	
	
	
	

	
	

}
