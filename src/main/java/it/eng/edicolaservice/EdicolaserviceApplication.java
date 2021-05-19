package it.eng.edicolaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.eng.edicolaservice.security.RSAKeys;
import it.eng.edicolaservice.service.user.UserDetailsServiceImpl;

@SpringBootApplication
public class EdicolaserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdicolaserviceApplication.class, args);

	}
	
	
	   @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

}
