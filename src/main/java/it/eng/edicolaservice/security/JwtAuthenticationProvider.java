package it.eng.edicolaservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;


public class JwtAuthenticationProvider implements AuthenticationProvider {

    Logger logger = LoggerFactory.getLogger(JwtAuthenticationProvider.class);

    private static final OAuth2Error DEFAULT_INVALID_TOKEN =
            invalidToken("An error occurred while attempting to decode the Jwt: Invalid token");
    
    private RSAKeys rsaKeys;  

    private final JwtAuthenticationConverter converter;


    public JwtAuthenticationProvider(JwtAuthenticationConverter converter, RSAKeys rsaKeys) {
        this.converter = converter;
        this.rsaKeys = rsaKeys;
    }

    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.getrPubKey()).build(); 
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Authenticating with custom AuthenticationProvider ...");
        BearerTokenAuthenticationToken bearerToken = (BearerTokenAuthenticationToken) authentication;

        String kid = null;
        try {
        	
            DecodedJWT jwt = JWT.decode(bearerToken.getToken());
            kid = jwt.getKeyId();
            
            logger.info("ISSUER:" + jwt.getIssuer());
            
        } catch (JWTDecodeException exception) {
            logger.info(exception.getMessage());
        }
        if (kid == null) {
            logger.info("JWT header does not contain a key ID / JWK Set URL. Provided token must be a static token.");
            logger.info("Trying to authenticate ...");

            BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
            Jwt jwt;
            try {
                jwt = jwtDecoder().decode(bearer.getToken());
            } catch (JwtException failed) {
                OAuth2Error invalidToken = invalidToken(failed.getMessage());
                logger.info(invalidToken.getDescription());
                throw new OAuth2AuthenticationException(invalidToken, invalidToken.getDescription(), failed);
            }
            AbstractAuthenticationToken token = this.converter.convert(jwt);
            logger.info("Successfully authenticated user with static token. (principal: {})", token.getPrincipal());
            return token;
        } else {
            logger.info("JWT header contains a key ID. Skipping static jwt verification ...");
            return null;
        }
    }

    private static OAuth2Error invalidToken(String message) {
        try {
            return new BearerTokenError(
                    BearerTokenErrorCodes.INVALID_TOKEN,
                    HttpStatus.UNAUTHORIZED,
                    message,
                    "https://tools.ietf.org/html/rfc6750#section-3.1");
        } catch (IllegalArgumentException malformed) {
            // some third-party library error messages are not suitable for RFC 6750's error message charset
            return DEFAULT_INVALID_TOKEN;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
//        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
        return authentication.equals(BearerTokenAuthenticationToken.class);
    }
}