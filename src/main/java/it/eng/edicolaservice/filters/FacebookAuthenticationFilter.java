package it.eng.edicolaservice.filters;

import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.UrlPathHelper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.edicolaservice.config.SecurityConstants;
import it.eng.edicolaservice.security.RSAKeys;
import it.eng.edicolaservice.security.Token;

public class FacebookAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private RSAKeys rsaKeys;

	private AuthenticationManager authenticationManager;

	private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(
			"/authenticate", "POST");

	public FacebookAuthenticationFilter(AuthenticationManager authenticationManager, RSAKeys rsaKeys) {
		super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
		this.authenticationManager = authenticationManager;
		this.rsaKeys = rsaKeys;
	}

	private static final Logger log = LoggerFactory.getLogger(FacebookAuthenticationFilter.class);
	private final static UrlPathHelper urlPathHelper = new UrlPathHelper();

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		log.info("attempting auth in FacebookAUthFilter");

		try {

			Token t = new ObjectMapper().readValue(req.getInputStream(), Token.class);

			log.info("access_token: " + t.getToken());

			return authenticationManager.authenticate(new BearerTokenAuthenticationToken(t.getToken()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException {

		String token = JWT.create().withSubject(((UserDetails) auth.getPrincipal()).getUsername())
				.withClaim("name", ((UserDetails) auth.getPrincipal()).getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.sign(Algorithm.RSA256(rsaKeys.getrPubKey(), rsaKeys.getrPriKey()));

		JSONObject body = new JSONObject();
		body.put("username", ((UserDetails) auth.getPrincipal()).getUsername());
		body.put("profile", new JSONObject().put("name", ((UserDetails) auth.getPrincipal()).getUsername()));
		body.put("id_token", token);

		res.getWriter().write(body.toString());
		res.getWriter().flush();
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		logger.debug("failed authentication while attempting to access "
				+ urlPathHelper.getPathWithinApplication((HttpServletRequest) request));

		// Add more descriptive message
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
	}

}