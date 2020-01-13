package org.yenbo.jetty.cxf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.rs.security.oauth2.grants.code.AuthorizationCodeGrantHandler;
import org.apache.cxf.rs.security.oauth2.grants.refresh.RefreshTokenGrantHandler;
import org.apache.cxf.rs.security.oauth2.provider.AccessTokenGrantHandler;
import org.apache.cxf.rs.security.oauth2.provider.DefaultResourceOwnerNameProvider;
import org.apache.cxf.rs.security.oauth2.provider.ResourceOwnerNameProvider;
import org.apache.cxf.rs.security.oauth2.services.AccessTokenService;
import org.apache.cxf.rs.security.oauth2.services.AuthorizationCodeGrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yenbo.jetty.api.DemoLoginService;
import org.yenbo.jetty.data.InMemoryUser;
import org.yenbo.jetty.oauth2.DemoDynamicRegistrationService;
import org.yenbo.jetty.oauth2.DemoSubjectCreator;
import org.yenbo.jetty.oauth2.DynamicRegistrationOAuthDataProvider;
import org.yenbo.jetty.oauth2.InMemoryAuthorizationCodeDataProvider;
import org.yenbo.jetty.oauth2.OAuthExtensionJSONProvider;
import org.yenbo.jetty.thymeleaf.OAuth2LoginViewMessageBodyWriter;
import org.yenbo.jetty.thymeleaf.OAuthAuthorizationDataMessageBodyWriter;
import org.yenbo.jetty.thymeleaf.OAuthErrorMessageBodyWriter;

@Configuration
public class Oauth2Configuration {
	
	private static final boolean PARTIAL_MATCH_SCOPE_VALIDATION = true;
	private static final boolean CAN_SUPPORT_PUBLIC_CLIENTS = false;
	private static final long GRANT_LIFE_TIME = 360L;
	public static final boolean SUPPORT_REGISTRATION_ACCESS_TOKEN = true;

	// OAuth 2.0 related
	@Autowired
	private InMemoryAuthorizationCodeDataProvider inMemoryAuthorizationCodeDataProvider;
	@Autowired
	private ResourceOwnerNameProvider resourceOwnerNameProvider;
	@Autowired
	private DemoSubjectCreator demoSubjectCreator;
	@Autowired
	private AuthorizationCodeGrantService authorizationCodeGrantService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private DynamicRegistrationOAuthDataProvider dynamicRegistrationOAuthDataProvider;
	@Autowired
	private DemoDynamicRegistrationService dynamicRegistrationService;
	
	// spring security related
	@Autowired
	private DemoLoginService demoLoginService;

	// writers
	@Autowired
	private OAuthAuthorizationDataMessageBodyWriter oAuthAuthorizationDataMessageBodyWriter;
	@Autowired
	private OAuthErrorMessageBodyWriter oAuthErrorMessageBodyWriter;
	@Autowired
	private OAuth2LoginViewMessageBodyWriter oAuth2LoginViewMessageBodyWriter;
	
	@Bean
	public DemoLoginService demoLoginService() {
		return new DemoLoginService();
	}
	
	@Bean
	public InMemoryAuthorizationCodeDataProvider inMemoryAuthorizationCodeDataProvider() {
		
		InMemoryAuthorizationCodeDataProvider dataProvider =
				new InMemoryAuthorizationCodeDataProvider();
		dataProvider.setGrantLifetime(GRANT_LIFE_TIME);
		return dataProvider;
	}
	
	@Bean
	public ResourceOwnerNameProvider resourceOwnerNameProvider() {
		// return login name on UI
		return new DefaultResourceOwnerNameProvider();
	}
	
	@Bean
    public AuthorizationCodeGrantService authorizationCodeGrantService() {
		
    	AuthorizationCodeGrantService service = new AuthorizationCodeGrantService();
    	service.setCanSupportPublicClients(CAN_SUPPORT_PUBLIC_CLIENTS);
    	service.setDataProvider(inMemoryAuthorizationCodeDataProvider);
    	service.setResourceOwnerNameProvider(resourceOwnerNameProvider);
    	service.setPartialMatchScopeValidation(PARTIAL_MATCH_SCOPE_VALIDATION);
    	service.setSubjectCreator(demoSubjectCreator);
    	return service;
    }
	
	@Bean
	public AccessTokenService accessTokenService() {
		
		RefreshTokenGrantHandler refreshTokenGrantHandler = new RefreshTokenGrantHandler();
		refreshTokenGrantHandler.setDataProvider(inMemoryAuthorizationCodeDataProvider);
		refreshTokenGrantHandler.setPartialMatchScopeValidation(PARTIAL_MATCH_SCOPE_VALIDATION);
		
		AuthorizationCodeGrantHandler authorizationCodeGrantHandler = new AuthorizationCodeGrantHandler();
		authorizationCodeGrantHandler.setCanSupportPublicClients(CAN_SUPPORT_PUBLIC_CLIENTS);
		authorizationCodeGrantHandler.setDataProvider(inMemoryAuthorizationCodeDataProvider);
		authorizationCodeGrantHandler.setPartialMatchScopeValidation(PARTIAL_MATCH_SCOPE_VALIDATION);
		
		List<AccessTokenGrantHandler> handlers = new ArrayList<>();
		handlers.add(authorizationCodeGrantHandler);
		handlers.add(refreshTokenGrantHandler);
		
		AccessTokenService service = new AccessTokenService();
		service.setDataProvider(inMemoryAuthorizationCodeDataProvider);
		service.setGrantHandlers(handlers);
		service.setWriteCustomErrors(true);
		return service;
	}
    
	@Bean
	public DynamicRegistrationOAuthDataProvider dynamicRegistrationOAuthDataProvider() {
		return new DynamicRegistrationOAuthDataProvider();
	}
	
	@Bean
	public DemoDynamicRegistrationService dynamicRegistrationService() {
		
		DemoDynamicRegistrationService dynamicRegistrationService = new DemoDynamicRegistrationService();
		dynamicRegistrationService.setClientProvider(dynamicRegistrationOAuthDataProvider);
		dynamicRegistrationService.setUserRole(InMemoryUser.ROLE_ADMIN);
		dynamicRegistrationService.setSupportRegistrationAccessTokens(SUPPORT_REGISTRATION_ACCESS_TOKEN);
		
		return dynamicRegistrationService;
	}
	
	@Bean
    public Server oauth2Server() {
		
        return CxfConfiguration.createServerFactory(new Oauth2Application(),
        		Arrays.<Object>asList(
        				new OAuthExtensionJSONProvider(),
        				oAuthAuthorizationDataMessageBodyWriter,
        				oAuthErrorMessageBodyWriter,
        				oAuth2LoginViewMessageBodyWriter
        				),
        		Arrays.<Object>asList(
                		authorizationCodeGrantService,
                		accessTokenService,
                		dynamicRegistrationService,
                		demoLoginService
                		)
        		);
    }
	
	@Bean
    public OAuthAuthorizationDataMessageBodyWriter oAuthAuthorizationDataMessageBodyWriter() {
    	return new OAuthAuthorizationDataMessageBodyWriter();
    }
    
    @Bean
    public OAuthErrorMessageBodyWriter oAuthErrorMessageBodyWriter() {
    	return new OAuthErrorMessageBodyWriter();
    }
    
    @Bean
    public OAuth2LoginViewMessageBodyWriter oAuth2LoginViewMessageBodyWriter() {
    	return new OAuth2LoginViewMessageBodyWriter();
    }
    
    @Bean
    public DemoSubjectCreator demoSubjectCreator() {
    	return new DemoSubjectCreator();
    }
}
