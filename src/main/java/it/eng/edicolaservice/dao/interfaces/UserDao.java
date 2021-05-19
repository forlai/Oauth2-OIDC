package it.eng.edicolaservice.dao.interfaces;

import java.util.List;

import it.eng.edicolaservice.dto.UserDto;

public interface UserDao {
	
	UserDto getUserById(String id);
	
	UserDto getUserBySub(String sub);
	
	UserDto getUser(String username);
	
	boolean createUser(UserDto user);	
	
	List<UserDto> getUsers();
	
	boolean updateUser(String id, UserDto user);
	
	boolean deleteUser(String id);

}

