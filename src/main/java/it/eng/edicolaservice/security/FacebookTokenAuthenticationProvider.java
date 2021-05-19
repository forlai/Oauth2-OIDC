package it.eng.edicolaservice.security;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.util.Assert;


public final class FacebookTokenAuthenticationProvider implements AuthenticationProvider {

	private final Log logger = LogFactory.getLog(getClass());

	private FacebookOpaqueTokenIntrospector introspector;
	private final Converter<OAuth2User, AbstractAuthenticationToken> converter;


	public FacebookTokenAuthenticationProvider(FacebookOpaqueTokenIntrospector introspector,
			Converter<OAuth2User, AbstractAuthenticationToken> converter) {
		Assert.notNull(introspector, "introspector cannot be null");
		this.introspector = introspector;
		this.converter = converter;
	}

	/**
	 * Introspect and validate the token

	 * @param authentication the authentication request object.
	 * @return A successful authentication
	 * @throws AuthenticationException if authentication failed for some reason
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof BearerTokenAuthenticationToken)) {
			return null;
		}
		BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
		DefaultOAuth2User authenticatedUser = getOAuth2AuthenticatedUser(bearer);
		AbstractAuthenticationToken result = this.converter.convert(authenticatedUser);
		result.setDetails(bearer.getDetails());
		this.logger.debug("Authenticated token");
		return result;
	}

	private DefaultOAuth2User getOAuth2AuthenticatedUser(BearerTokenAuthenticationToken bearer) {
		try {
			return this.introspector.introspect(bearer.getToken());
		}
		catch (BadOpaqueTokenException failed) {
			this.logger.debug("Failed to authenticate since token was invalid");
			throw new InvalidBearerTokenException(failed.getMessage());
		}
		catch (OAuth2IntrospectionException failed) {
			throw new AuthenticationServiceException(failed.getMessage());
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}



}
