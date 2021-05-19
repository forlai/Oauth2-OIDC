package it.eng.edicolaservice.service.user;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.eng.edicolaservice.dao.interfaces.UserDao;
import it.eng.edicolaservice.dto.UserDto;

@Service
public class OidcUserDetailsServiceImpl implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(OidcUserDetailsServiceImpl.class);

	@Autowired
	UserDao userDaoImpl;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	//effettuiamo ricerca con il claim sub come chiave, sub identifica univocamente lo user
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			UserDto user = userDaoImpl.getUserBySub(username);
			log.info("FOUND : " + user.getUsername() + user.getPassword());

			UserDetails userd = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
			return userd;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new UsernameNotFoundException(e.getMessage());
		}

	};

	public UserDto createUser(String sub, String issuer, String username, String password, String firstName, String lastName, String email) {

		log.info("creating user with: " + username + " " + password + " " + firstName + " " + lastName + " " + email);
		if (passwordEncoder == null) {
			log.info("passordEncoder is null");
		}
		UserDto user = new UserDto(sub, issuer, username, password, firstName, lastName, email);
		boolean r = userDaoImpl.createUser(user);

		log.info("Creazione user: " + r);

		return user;

	}


	public UserDto getUserById(String id) {
		return userDaoImpl.getUserById(id);
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

}
