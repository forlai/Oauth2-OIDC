package it.eng.edicolaservice.service.user;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.eng.edicolaservice.dao.UserDaoImpl;
import it.eng.edicolaservice.dao.UserDaoImpl.UserMapper;
import it.eng.edicolaservice.dao.interfaces.UserDao;
import it.eng.edicolaservice.dto.UserDto;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	
	@Autowired
	UserDao userDaoImpl;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {

			log.info("looking for user: " + username);
			UserDto user = userDaoImpl.getUser(username);

			log.info("FOUND : " + user.getUsername() + user.getPassword());

			UserDetails userd = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
			return userd;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new UsernameNotFoundException(e.getMessage());
		}

	};

	public UserDto createUser(String sub, String issuer, String username, String password, String firstName, String lastName, String email) {


		UserDto user = new UserDto(sub, issuer, username, passwordEncoder.encode(password), firstName, lastName, email);
		boolean r = userDaoImpl.createUser(user);

		log.info("Creazione user: " + r);

		return user;

	}

	public boolean updateUser(String id, String username, String password, String firstName, String lastName,
			String email) {

		UserDto user = new UserDto("", "auto", username, passwordEncoder.encode(password), firstName, lastName, email);
		boolean r = userDaoImpl.updateUser(id, user);

		log.info("Creazione user: " + r);

		return r;

	}

	
	public boolean deleteUser(String id) {

		boolean r = userDaoImpl.deleteUser(id);

		log.info("Cancellazione user ID: " + id +" : " +r);

		return r;

	}	
	
	
	public List<UserDto> getAllUsers() {
		return userDaoImpl.getUsers();

	}

	public UserDto getUserById(String id) {
		return userDaoImpl.getUserById(id);
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	
	
	
}
