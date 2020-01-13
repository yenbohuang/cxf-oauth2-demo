package org.yenbo.jetty.api;

import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.yenbo.jetty.data.InMemoryClient;
import org.yenbo.jetty.repo.ClientRepository;
import org.yenbo.jetty.security.SpringSecurityUtils;
import org.yenbo.jetty.view.OAuth2LoginView;

@Component
@Path("/login")
public class DemoLoginService {

	private static final Logger log = LoggerFactory.getLogger(DemoLoginService.class);
	
	@Autowired
	private ClientRepository clientRepository;
	
	private String getAppName(InMemoryClient client, String langTag) {
		
		String name = client.getName();
		
		if (client.getNameI18nMap().containsKey(langTag)) {
			name = client.getNameI18nMap().get(langTag);
		}
		
		return name;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response login(
			@QueryParam("error") String error,
			@QueryParam("logout") String logout,
			@QueryParam(OAuthConstants.CLIENT_ID) String clientIdFromQueryString,
			@Context MessageContext messageContext) {

		log.debug("Generate customized login form: error={}, logout={}", error, logout);
		
		String clientId = clientIdFromQueryString;
		
		SavedRequest savedRequest = SpringSecurityUtils.getSavedRequest(messageContext);

		if (null != savedRequest) {
			clientId = UriComponentsBuilder
					.fromUriString(savedRequest.getRedirectUrl()).build()
					.getQueryParams().getFirst(OAuthConstants.CLIENT_ID);
		}
		
		Locale userLocale = messageContext.getHttpServletRequest().getLocale();
		
		OAuth2LoginView view = new OAuth2LoginView();
		view.setError(error != null);
		view.setLogout(logout != null);
		view.setLocale(userLocale);
		
		if (null != clientId) {
			
			InMemoryClient client = clientRepository.get(UUID.fromString(clientId));
			
			if (null != client) {
				view.setAppName(getAppName(client, userLocale.toLanguageTag()));
			}
		}
		
		return Response.ok().entity(view).build();
	}
}
