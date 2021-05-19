package it.eng.edicolaservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

import it.eng.edicolaservice.dto.UserDto;
import it.eng.edicolaservice.service.user.OidcUserDetailsServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class OidcJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	Logger logger = LoggerFactory.getLogger(OidcJwtAuthenticationConverter.class);

	private static final String GROUPS_CLAIM = "groups";
	private static final String ROLE_PREFIX = "ROLE_";
	private final UserDetailsService userDetailsService;

	public OidcJwtAuthenticationConverter(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {

		// token proveniente da OIDC auth server -> usiamo sub come username univoco
		String sub = jwt.getClaimAsString("sub");
		UserDetails userDetails = null;
		UserDto userDto = null;

		try {
			userDetails = userDetailsService.loadUserByUsername(sub);
		} catch (UsernameNotFoundException e) {

			// l'utente oidc non è già registrato, lo si registra
			userDto = ((OidcUserDetailsServiceImpl) userDetailsService).createUser(sub, jwt.getIssuer().toString(), 
					jwt.getClaimAsString("email")==null?jwt.getClaimAsString("family_name"):jwt.getClaimAsString("email")
					,"", jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"),
					jwt.getClaimAsString("email"));

			userDetails = new User(userDto.getUsername(), userDto.getPassword(), new ArrayList<>());

		}

		Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
		logger.info("Adding ROLE_BASIC to user {}", userDetails.getUsername());
		authorities.add(new SimpleGrantedAuthority("ROLE_BASIC"));
		return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), "n/a", authorities);

	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		return this.getGroups(jwt).stream().map(authority -> ROLE_PREFIX + authority.toUpperCase())
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private Collection<String> getGroups(Jwt jwt) {
		Object groups = jwt.getClaims().get(GROUPS_CLAIM);
		if (groups instanceof Collection) {
			return (Collection<String>) groups;
		}
		return Collections.emptyList();
	}
}