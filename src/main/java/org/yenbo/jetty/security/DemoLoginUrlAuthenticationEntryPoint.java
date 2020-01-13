package org.yenbo.jetty.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

public class DemoLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(DemoLoginUrlAuthenticationEntryPoint.class);
	
	public DemoLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		
		log.debug("request.getParameterMap()={}", request.getParameterMap());
		String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
		
		String redirectUrl = super.determineUrlToUseForThisRequest(request, response, exception);
		
		if (null != clientId) {
			return UriComponentsBuilder
					.fromPath(redirectUrl)
					.queryParam(OAuthConstants.CLIENT_ID, clientId)
					.toUriString();
		} else {
			return redirectUrl;
		}
	}
}
