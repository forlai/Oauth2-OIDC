package it.eng.edicolaservice.config;


import it.eng.edicolaservice.filters.FacebookAuthenticationFilter;
import it.eng.edicolaservice.filters.JWTAuthenticationFilter;
import it.eng.edicolaservice.security.FacebookAuthenticationConverter;
import it.eng.edicolaservice.security.FacebookEntityConverter;
import it.eng.edicolaservice.security.FacebookOpaqueTokenIntrospector;
import it.eng.edicolaservice.security.FacebookTokenAuthenticationProvider;
import it.eng.edicolaservice.security.JwtAuthenticationConverter;
import it.eng.edicolaservice.security.JwtAuthenticationProvider;
import it.eng.edicolaservice.security.OidcJwtAuthenticationConverter;
import it.eng.edicolaservice.security.RSAKeys;
import it.eng.edicolaservice.service.user.OidcUserDetailsServiceImpl;
import it.eng.edicolaservice.service.user.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;


@Configuration
@EnableWebSecurity

public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JWTClaimsSetAwareJWSKeySelector<SecurityContext> tenantJWSKeySelector;
    private final OAuth2TokenValidator<Jwt> tenantJwtIssuerValidator;
    //private final UserDetailsService userDetailsServiceImpl;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    OidcUserDetailsServiceImpl oauthUserDetailsServiceImpl;    
    
    @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri}")    
    String introspectionUri; 
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id}")
    String clientId; 
    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret}")
    String clientSecret;

    public WebSecurityConfig(JWTClaimsSetAwareJWSKeySelector<SecurityContext> tenantJWSKeySelector,
    		OAuth2TokenValidator<Jwt> tenantJwtIssuerValidator) {
        this.tenantJWSKeySelector = tenantJWSKeySelector;
        this.tenantJwtIssuerValidator = tenantJwtIssuerValidator;        
    }
	

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        .headers().frameOptions().sameOrigin().and() //TODO rimuovere, serve solo per console h2
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()        
        .cors().and()
        .csrf().disable() //TODO rimuovere, serve solo per chiamate da postman    
        .httpBasic().and()
        .authorizeRequests(authorize -> authorize

    		.antMatchers("/h2-console/**").permitAll() //TODO rimuovere, serve solo per console h2        		
            .antMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL).permitAll()
            .antMatchers(HttpMethod.POST, "/users/login").permitAll()
            .antMatchers(HttpMethod.POST, "/authenticate").permitAll()
            .antMatchers(HttpMethod.POST, "/users/register").permitAll()
            .anyRequest().authenticated()            
        )
        //login da form: autentica user ed emette JWT
        .addFilter(new JWTAuthenticationFilter(authenticationManager(), rsaKeys())) 
        
        //login con access_token proveniente da facebook
        .addFilterBefore(new FacebookAuthenticationFilter(authenticationManager(), rsaKeys()), JWTAuthenticationFilter.class)

        //oidc autentication multitenant
        .oauth2ResourceServer(oauth2 -> oauth2
        		.jwt().jwtAuthenticationConverter(oAuthJwtAuthenticationConverter()).decoder(jwtDecoder())
        );


        
        
    }

    
    
    @Override    
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {    	
        auth.authenticationProvider(jwtAuthenticationProvider());
        auth.authenticationProvider(facebookTokenAuthenticationProvider());
        auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder);        
 
    }
    
    @Bean
    AuthenticationProvider facebookTokenAuthenticationProvider() {
        return new FacebookTokenAuthenticationProvider(facebookTokenIntrospector(), 
        		new FacebookAuthenticationConverter(oauthUserDetailsServiceImpl));
    }  
    
    @Bean
    public FacebookOpaqueTokenIntrospector facebookTokenIntrospector() {
        //return new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    	return new FacebookOpaqueTokenIntrospector(introspectionUri, new RestTemplate(), facebookEntityConverter());    	
    }    
    
    @Bean
    FacebookEntityConverter facebookEntityConverter() {
        return new FacebookEntityConverter(introspectionUri);
    }      
    
    @Bean
    JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(new JwtAuthenticationConverter(userDetailsServiceImpl), rsaKeys());
    }   
    
    @Bean
    OidcJwtAuthenticationConverter oAuthJwtAuthenticationConverter() {
        return new OidcJwtAuthenticationConverter((UserDetailsService) oauthUserDetailsServiceImpl);
    }        
    
      
    
    @Bean
    JWTProcessor<SecurityContext> jwtProcessor() {    
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<SecurityContext>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(tenantJWSKeySelector);
        return jwtProcessor;
    }    
    

    
    @Bean
    JwtDecoder jwtDecoder() {    
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor());
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>
                (JwtValidators.createDefault(), tenantJwtIssuerValidator);
        decoder.setJwtValidator(validator);
        return decoder;
    }    
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);       
        
        return source;
    }    
	


	
	@Bean public RSAKeys rsaKeys() {
	    return new RSAKeys(); 
	}
	

 
}