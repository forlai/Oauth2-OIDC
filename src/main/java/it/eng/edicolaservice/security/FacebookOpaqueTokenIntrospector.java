package it.eng.edicolaservice.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import net.minidev.json.JSONObject;

import com.google.gson.Gson;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;



public class FacebookOpaqueTokenIntrospector /*extends NimbusOpaqueTokenIntrospector*/ {
	
	private RestOperations restOperations;	
	private Converter<String, RequestEntity<?>> requestEntityConverter;	
	
	/**
	 * Creates a {@code OpaqueTokenAuthenticationProvider} with the provided parameters
	 * @param introspectionUri The introspection endpoint uri
	 * @param clientId The client id authorized to introspect
	 * @param clientSecret The client's secret
	 */
	/*
	public FacebookOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret, Converter<String, RequestEntity<?>> reqEntityConverter) {		
		//super(introspectionUri, clientId, clientSecret);
		this.setRequestEntityConverter(reqEntityConverter); 	
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
		this.restOperations = restTemplate;		
	}
	*/

	/**
	 * Creates a {@code OpaqueTokenAuthenticationProvider} with the provided parameters
	 *
	 * The given {@link RestOperations} should perform its own client authentication
	 * against the introspection endpoint.
	 * @param introspectionUri The introspection endpoint uri
	 * @param restOperations The client for performing the introspection request
	 */
	public FacebookOpaqueTokenIntrospector(String introspectionUri, RestOperations restOperations, Converter<String, RequestEntity<?>> reqEntityConverter) {
		//super(introspectionUri, restOperations);
		this.setRequestEntityConverter(reqEntityConverter); 		
		this.restOperations = restOperations;

	}	
	
	
	/*****/
	
	
	//@Override
	public DefaultOAuth2User introspect(String token) {
		RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
		if (requestEntity == null) {
			throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
		}
		ResponseEntity<String> responseEntity = makeRequest(requestEntity);
		

		//todo: controllare equivalenza client id
		
		if (responseEntity.getStatusCodeValue() != 200) {
			throw new OAuth2IntrospectionException("Introspection endpoint responded with " + responseEntity.getStatusCodeValue());
		}
		
		return convertClaimsSet(responseEntity);
	}	
	
	
	
	private ResponseEntity<String> makeRequest(RequestEntity<?> requestEntity) {
		try {
			return this.restOperations.exchange(requestEntity, String.class);
		}
		catch (Exception ex) {
			throw new OAuth2IntrospectionException(ex.getMessage(), ex);
		}
	}	
	
	/**
	 * Sets the {@link Converter} used for converting the OAuth 2.0 access token to a
	 * {@link RequestEntity} representation of the OAuth 2.0 token introspection request.
	 * @param requestEntityConverter the {@link Converter} used for converting to a
	 * {@link RequestEntity} representation of the token introspection request
	 */
	public void setRequestEntityConverter(Converter<String, RequestEntity<?>> requestEntityConverter) {
		Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
		this.requestEntityConverter = requestEntityConverter;
	}
	
	
	private DefaultOAuth2User convertClaimsSet(ResponseEntity<String> responseEntity) {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		
		//todo vedere se si toglie questo warning		
		Map<String, Object> claims = new Gson().fromJson(responseEntity.getBody(), HashMap.class); 
				//((String) responseEntity.getBody()). .toJSONObject();
		
		DefaultOAuth2User principal = new DefaultOAuth2User(authorities, claims, "name");
		
		return principal;
		
	}
	
	
	
	/*
	private HTTPResponse adaptToNimbusResponse(ResponseEntity<String> responseEntity) {
		HTTPResponse response = new HTTPResponse(responseEntity.getStatusCodeValue());
		response.setHeader(HttpHeaders.CONTENT_TYPE, responseEntity.getHeaders().getContentType().toString());
		response.setContent(responseEntity.getBody());
		if (response.getStatusCode() != HTTPResponse.SC_OK) {
			throw new OAuth2IntrospectionException("Introspection endpoint responded with " + response.getStatusCode());
		}
		return response;
	}	
*/

}
