package it.eng.edicolaservice.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import it.eng.edicolaservice.service.user.UserDetailsServiceImpl;
import it.eng.edicolaservice.dao.interfaces.UserDao;
import it.eng.edicolaservice.dto.UserDto;

@Component
public class UserDaoImpl implements UserDao {
	
	private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	private final String SQL_GET_USER_BYID = "select * from euser where id = ?";
	private final String SQL_GET_USER_BYSUB = "select * from euser where sub = ?";
	private final String SQL_GET_USER = "select * from euser where username = ?";
	private final String SQL_GET_USERS = "select * from euser";
	private final String SQL_INS_USER = "insert into euser (SUB, ISSUER, USERNAME, PASSWORD, FIRSTNAME, LASTNAME, EMAIL) values (?,?,?,?,?,?,?)";
	private final String SQL_UPD_USER = "update euser set USERNAME=?, PASSWORD=?, FIRSTNAME=?, LASTNAME=?, EMAIL=? where ID = ?";
	private final String SQL_DEL_USER = "delete from euser where ID = ?";

	
	
	public UserDto getUserById(String id) {
		UserMapper userMapper = new UserMapper();
		return jdbcTemplate.queryForObject(SQL_GET_USER_BYID, userMapper, id);
	}
	
	public UserDto getUserBySub(String sub) {
		UserMapper userMapper = new UserMapper();
		return jdbcTemplate.queryForObject(SQL_GET_USER_BYSUB, userMapper, sub);
	}	

	@Override
	public UserDto getUser(String username) {
		UserMapper userMapper = new UserMapper();
		return jdbcTemplate.queryForObject(SQL_GET_USER, userMapper, username);		
		
	};	
	
	@Override
	public boolean createUser(UserDto user) {
		boolean result = false;
		
		try {
			int i =0;
			i = jdbcTemplate.update(SQL_INS_USER, user.getSub(), user.getIssuer(), user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(),
					user.getEmail());
			
			result = (i>0);				
			
		} catch (DataAccessException d) {
			
			log.error(d.getMessage(), d);
			result=false;
		}
		
		return result;

	}
	
	public boolean updateUser(String id, UserDto user) {
		boolean result = false;		
		
		try {
			int i =0;
			i = jdbcTemplate.update(SQL_UPD_USER, user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(),
					user.getEmail(), Integer.parseInt(id));
			
			result = (i>0);				
			
		} catch (DataAccessException d) {			
			log.error(d.getMessage());
			throw d;
		}		
		
		return result;
	};
	
	public boolean deleteUser(String id) {
		boolean result = false;		
		
		try {
			int i =0;
			i = jdbcTemplate.update(SQL_DEL_USER, Integer.parseInt(id));
			
			result = (i>0);				
			
		} catch (DataAccessException d) {			
			log.error(d.getMessage());
			throw d;
		}		
		
		return result;
	};		
	
	public List<UserDto> getUsers(){
		try{
			log.info("calling getusers");
			List<UserDto> uList = jdbcTemplate.query(SQL_GET_USERS, new UserMapper());
			log.info("getusers result: " + (uList==null?0:uList.size()));
			return uList;
		}
		catch(Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}


	
	public class UserMapper implements RowMapper<UserDto> {
		   public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserDto user = new UserDto();
				user.setId(rs.getInt("ID"));
				user.setSub(rs.getString("SUB"));
				user.setIssuer(rs.getString("ISSUER"));
				user.setUsername(rs.getString("USERNAME"));
				user.setPassword(rs.getString("PASSWORD"));
				user.setFirstName(rs.getString("FIRSTNAME"));
				user.setLastName(rs.getString("LASTNAME"));
				user.setEmail(rs.getString("EMAIL"));

				return user;
		   }
		}	
	

}
