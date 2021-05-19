package it.eng.edicolaservice.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import it.eng.edicolaservice.dto.UserDto;
import it.eng.edicolaservice.service.user.OidcUserDetailsServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FacebookAuthenticationConverter implements Converter<OAuth2User, AbstractAuthenticationToken> {

    Logger logger = LoggerFactory.getLogger(FacebookAuthenticationConverter.class);

    private static final String GROUPS_CLAIM = "groups";
    private static final String ROLE_PREFIX = "ROLE_";
    private final UserDetailsService userDetailsService;

    public FacebookAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(OAuth2User facebookUser) {
		// user proveniente da Facebook info_point -> usiamo name come username univoco
		String sub = facebookUser.getAttribute("id");
		String name = facebookUser.getAttribute("name");
		String email = facebookUser.getAttribute("email");
		UserDetails userDetails = null;
		UserDto userDto = null;

		try {
			userDetails = userDetailsService.loadUserByUsername(sub);
		} catch (UsernameNotFoundException e) {

			// l'utente non è già registrato, lo si registra
			String[] first_secondName = name.split("\\s+");
			userDto = ((OidcUserDetailsServiceImpl) userDetailsService).createUser(sub, "facebook", email==null?first_secondName[1]:email,
					"", first_secondName[0], first_secondName[1], email);		

			userDetails = new User(userDto.getUsername(), userDto.getPassword(), new ArrayList<>());

		}

		Collection<GrantedAuthority> authorities = 	extractAuthorities(facebookUser);
		logger.info("Adding ROLE_BASIC to user {}", userDetails.getUsername());
		authorities.add(new SimpleGrantedAuthority("ROLE_BASIC"));
		return new UsernamePasswordAuthenticationToken(userDetails, "n/a", authorities);

	}

    private Collection<GrantedAuthority> extractAuthorities(OAuth2User facebookUser) {
        return new ArrayList<GrantedAuthority>(facebookUser.getAuthorities());
        		/*
        		this.getGroups(jwt).stream()
                .map(authority -> ROLE_PREFIX + authority.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
                */
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