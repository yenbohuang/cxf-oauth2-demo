package org.yenbo.jetty.thymeleaf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.yenbo.jetty.repo.ScopeRepository;

@Provider
@Produces("text/html")
public class OAuthAuthorizationDataMessageBodyWriter
	extends AbstractThymeleafMessageBodyWriter<OAuthAuthorizationData> {

	@Autowired
	private ScopeRepository scopeRepository;
	
	public OAuthAuthorizationDataMessageBodyWriter() {
		super("OAuthAuthorizationData");
	}
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == OAuthAuthorizationData.class;
	}

	@Override
	protected Context createContext(OAuthAuthorizationData oAuthAuthorizationData) {
		
		// TODO get Locale from oAuthAuthorizationData
		Context context = new Context(Locale.ENGLISH);
		
		// TODO Form binding does not work on "th:field" with Spring web MVC.
		
		// required fields
		context.setVariable("response_type", oAuthAuthorizationData.getResponseType());
		context.setVariable("client_id", oAuthAuthorizationData.getClientId());
		context.setVariable("redirect_uri", oAuthAuthorizationData.getRedirectUri());
		context.setVariable("scope", oAuthAuthorizationData.getProposedScope());
		context.setVariable("state", oAuthAuthorizationData.getState());
		context.setVariable("session_authenticity_token", oAuthAuthorizationData.getAuthenticityToken());
		
		// Display only fields
		context.setVariable("appDescription", oAuthAuthorizationData.getApplicationDescription());
		context.setVariable("appName", oAuthAuthorizationData.getApplicationName());
		context.setVariable("endUserName", oAuthAuthorizationData.getEndUserName());
		context.setVariable("replyTo", oAuthAuthorizationData.getReplyTo());
		
		List<String> scopeDescriptions = new ArrayList<>();
		for (OAuthPermission permission: oAuthAuthorizationData.getPermissions()) {
			scopeDescriptions.add(scopeRepository.getScopeDescription(
					permission.getPermission(), Locale.ENGLISH));
		}
		context.setVariable("scopeDescriptions", scopeDescriptions);
		
		return context;
	}
}
