package it.eng.edicolaservice.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import it.eng.edicolaservice.dto.UserDto;
import it.eng.edicolaservice.filters.JWTAuthenticationFilter;
import it.eng.edicolaservice.service.user.UserDetailsServiceImpl;

@RestController
@RequestMapping("/")
public class UserController {

	@Autowired
	UserDetailsServiceImpl userDetailsServiceImpl;
	
	private static final Logger log = LoggerFactory.getLogger(UserController.class);	
	
	//create user	
	@RequestMapping(value = "/users/register" , method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public @ResponseBody UserDto userCreate(@RequestBody UserDto usr) {

		UserDto userCreated = null;// = new UserDto("aaa","","","","");
		String msg = "User creato con successo";

		try {
			userCreated=userDetailsServiceImpl.createUser("", "auto", usr.getUsername(), usr.getPassword(), usr.getFirstName(), usr.getLastName(), usr.getEmail());
		}
		catch(Exception e) {
			msg = e.getMessage();
			log.error(e.getMessage());
			throw e;
			
		}
			
		return userCreated;
		
	}

	//update user	
	@RequestMapping(value = "/users/update/{id}" , method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public @ResponseBody boolean userUpdate(@PathVariable("id") String id, @RequestBody UserDto usr) throws Exception {

		
		boolean res = false;
		String msg = "Update eseguito con Successo";

		try {
			res = userDetailsServiceImpl.updateUser(id, usr.getUsername(), usr.getPassword(), usr.getFirstName(), usr.getLastName(), usr.getEmail());
			
			if(!res)
				msg="Errore nell'esecuzione dell'update";
		}
		catch(Exception e) {
			log.error(e.getMessage());
			msg = e.getMessage();
			
		}
		
		if(!res)
			throw new Exception(msg);

		return res;
		
	}	
	
	//delete user	
	@RequestMapping(value = "/users/delete/{id}" , method = RequestMethod.GET)
	public @ResponseBody boolean userDel(@PathVariable("id") String id) throws Exception {

		
		boolean res = false;
		String msg = "Cancellazione eseguita con Successo";

		try {
			res = userDetailsServiceImpl.deleteUser(id);
			
			if(!res)
				msg="Errore nella cancellazione dell'utente";
		}
		catch(Exception e) {
			log.error(e.getMessage());
			msg = e.getMessage();
			
		}
		
		if(!res)
			throw new Exception(msg);

		return res;
		
	}		
	
	//user by id	
	@RequestMapping(value = "/users/{id}" , method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserDto userById(@PathVariable("id") String id) {

		UserDto user= null;

		try {
			user=userDetailsServiceImpl.getUserById(id);
		}
		catch(Exception e) {
			log.error(e.getMessage());
			
		}
		return user;
		
	}	
	
	//tutti gli user	
	@RequestMapping(value = "/users" , method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String userAll() {

		JSONArray jArray = null;
		
		try {
			
			List<UserDto> usersList = userDetailsServiceImpl.getAllUsers();
			log.info("Users estratti: " + usersList);
			
			jArray = new JSONArray(userDetailsServiceImpl.getAllUsers());
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			
		}
			
		return jArray.toString();
		
	}		
	
}

