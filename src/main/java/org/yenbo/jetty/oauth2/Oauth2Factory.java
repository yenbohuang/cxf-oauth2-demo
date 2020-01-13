package org.yenbo.jetty.oauth2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthError;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.grants.code.ServerAuthorizationCodeGrant;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistrationResponse;
import org.apache.cxf.rs.security.oauth2.tokens.bearer.BearerAccessToken;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;
import org.yenbo.jetty.data.InMemoryAccessToken;
import org.yenbo.jetty.data.InMemoryAuthorizationCode;
import org.yenbo.jetty.data.InMemoryClient;
import org.yenbo.jetty.data.InMemoryRefreshToken;

public class Oauth2Factory {

	public static final long ACCESS_TOKEN_EXPIRED_TIME_SECONDS = 12345L;
	public static final long REFRESH_TOKEN_EXPIRED_TIME_SECONDS = 67890L;
	public static final long CLIENT_SECRET_EXPIRES_AT_SECONDS = 0L;
	
	public static final String KEY_USER_PROPERTY = "USER_PROPERTY";
	
	public static final List<String> GRANT_TYPE_LIST = Arrays.<String>asList(
			OAuthConstants.AUTHORIZATION_CODE_GRANT,
			OAuthConstants.REFRESH_TOKEN);
	public static final List<String> RESPONSE_TYPE_LIST = Arrays.<String>asList(
			OAuthConstants.CODE_RESPONSE_TYPE,
			OAuthConstants.TOKEN_RESPONSE_TYPE);
	
	private Oauth2Factory() {
	}

	public static InMemoryAuthorizationCode create(ServerAuthorizationCodeGrant grant) {
		
		if (null == grant) {
			throw new IllegalArgumentException("grant is null");
		}
		
		InMemoryAuthorizationCode authorizationCode = new InMemoryAuthorizationCode();
		authorizationCode.getScopes().addAll(grant.getApprovedScopes());
		authorizationCode.setClientId(UUID.fromString(grant.getClient().getClientId()));
		authorizationCode.setCode(grant.getCode());
		authorizationCode.setExpiresIn(grant.getExpiresIn());
		authorizationCode.setIssuedAt(grant.getIssuedAt());
		authorizationCode.setRedirectUri(grant.getRedirectUri());
		
		// UserSubject
		authorizationCode.setUsername(grant.getSubject().getLogin());
		authorizationCode.setUserProperty(grant.getSubject().getProperties().get(KEY_USER_PROPERTY));
		
		return authorizationCode;
	}
	
	public static ServerAuthorizationCodeGrant create(Client client,
			InMemoryAuthorizationCode inMemoryAuthorizationCode) {
		
		if (null == inMemoryAuthorizationCode) {
			throw new IllegalArgumentException("inMemoryAuthorizationCode is null.");
		}
		
		if (null == client) {
			throw new IllegalArgumentException("client is null.");
		}
		
		ServerAuthorizationCodeGrant grant = new ServerAuthorizationCodeGrant();
		grant.getApprovedScopes().addAll(inMemoryAuthorizationCode.getScopes());
		grant.setClient(client);
		grant.setCode(inMemoryAuthorizationCode.getCode());
		grant.setExpiresIn(inMemoryAuthorizationCode.getExpiresIn());
		grant.setIssuedAt(inMemoryAuthorizationCode.getIssuedAt());
		grant.setRedirectUri(inMemoryAuthorizationCode.getRedirectUri());
		
		// UserSubject
		UserSubject subject = new UserSubject();
		subject.setLogin(inMemoryAuthorizationCode.getUsername());
		subject.getProperties().put(KEY_USER_PROPERTY, inMemoryAuthorizationCode.getUserProperty());
		grant.setSubject(subject);
		
		return grant;
	}
	
	public static Client createClient() {
		
		Client client = new Client();
		
		client.setConfidential(true);
		client.setTokenEndpointAuthMethod(OAuthConstants.TOKEN_ENDPOINT_AUTH_POST);
		client.setAllowedGrantTypes(GRANT_TYPE_LIST);
		
		return client;
	}
	
	public static Client create(InMemoryClient inMemoryClient) {
		
		if (null == inMemoryClient) {
			throw new IllegalArgumentException("inMemoryClient is null.");
		}
		
		Client client = createClient();
		
		client.setApplicationDescription(inMemoryClient.getDescription());
		client.setApplicationName(inMemoryClient.getName());
		client.setClientId(inMemoryClient.getClientId().toString());
		client.setClientSecret(inMemoryClient.getClientSecret());
		client.getRedirectUris().addAll(inMemoryClient.getRedirectUris());
		client.setRegisteredAt(inMemoryClient.getIssuedAt());
		client.getRegisteredScopes().addAll(inMemoryClient.getScopes());
		
		// client_name#<language tag>
		if (!inMemoryClient.getNameI18nMap().isEmpty()) {
			for (Entry<String, String> entry: inMemoryClient.getNameI18nMap().entrySet()) {
				client.getProperties().put(
						OAuthExtensionConstants.CLIENT_NAME_PREFIX + entry.getKey(),
						entry.getValue());
			}
		}
		    	
    	return client;
	}
	
	public static InMemoryClient create(Client client) {
		
		if (null == client) {
			throw new IllegalArgumentException("client is null.");
		}
		
		InMemoryClient inMemoryClient = new InMemoryClient();
		
		inMemoryClient.setClientId(UUID.fromString(client.getClientId()));
		inMemoryClient.setClientSecret(client.getClientSecret());
		inMemoryClient.setDescription(client.getApplicationDescription());
		inMemoryClient.setIssuedAt(client.getRegisteredAt());
		inMemoryClient.setName(client.getApplicationName());
		inMemoryClient.getRedirectUris().addAll(client.getRedirectUris());
		inMemoryClient.getScopes().addAll(client.getRegisteredScopes());
		
		// client_name#<language tag>
		for (Entry<String, String> entry: client.getProperties().entrySet()) {
			if (entry.getKey().startsWith(OAuthExtensionConstants.CLIENT_NAME_PREFIX)) {
				Locale locale = Locale.forLanguageTag(
						entry.getKey().split(OAuthExtensionConstants.CLIENT_NAME_SPLITTER)[1]);
				inMemoryClient.getNameI18nMap().put(locale.toLanguageTag(), entry.getValue());
			}
		}		
		
		return inMemoryClient;
	}
	
	private static void fillClientNameMap(ClientRegistration fromClientRegistration,
			Client toClient) {
		
		Map<String, Object> clientNameMap = fromClientRegistration.getMapProperty(
				OAuthExtensionConstants.CLIENT_NAME_I18N);
		
		if (null != clientNameMap && !clientNameMap.isEmpty()) {
			
			for (Entry<String, Object> entry: clientNameMap.entrySet()) {
				toClient.getProperties().put(entry.getKey(), (String) entry.getValue());
			}
		} else {
			toClient.getProperties().remove(OAuthExtensionConstants.CLIENT_NAME_I18N);
		}
	}
	
	public static Client fill(ClientRegistration fromClientRegistration, Client toClient,
			boolean createNewClient) {
		
		if (null == fromClientRegistration) {
			throw new IllegalArgumentException("fromClientRegistration is null.");
		}
		
		if (null == toClient) {
			throw new IllegalArgumentException("toClient is null.");
		}
		
		toClient.setApplicationDescription(fromClientRegistration.getStringProperty(
				OAuthExtensionConstants.CLIENT_DESCRIPTION));
		fillClientNameMap(fromClientRegistration, toClient);
		
		if (createNewClient) {
			// for create new client
		} else {
			// for update client
			toClient.setApplicationName(fromClientRegistration.getClientName());
			toClient.setRedirectUris(fromClientRegistration.getRedirectUris());
			
			// This is not compliant with RFC 7592
			if (fromClientRegistration.containsProperty(ClientRegistrationResponse.CLIENT_SECRET)) {
				toClient.setClientSecret(fromClientRegistration.getStringProperty(
						ClientRegistrationResponse.CLIENT_SECRET));
			}
			
			toClient.setRegisteredScopes(OAuthUtils.parseScope(fromClientRegistration.getScope()));
		}
		
		return toClient;
	}
	
	private static Map<String, String> createClientNameMap(Client fromClient) {
		
		// client_name#<language tag>
		Map<String, String> clientNameMap = new HashMap<>();
		
		for (Entry<String, String> entry: fromClient.getProperties().entrySet()) {
			if (entry.getKey().startsWith(OAuthExtensionConstants.CLIENT_NAME_PREFIX)) {
				clientNameMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return clientNameMap;
	}
	
	public static ClientRegistration fill(Client fromClient, ClientRegistration toClientRegistration) {
		
		if (null == fromClient) {
			throw new IllegalArgumentException("fromClient is null.");
		}
		
		if (null == toClientRegistration) {
			throw new IllegalArgumentException("toClientRegistration is null");
		}
		
		toClientRegistration.setProperty(OAuthExtensionConstants.CLIENT_DESCRIPTION,
				fromClient.getApplicationDescription());
		toClientRegistration.setProperty(ClientRegistrationResponse.CLIENT_ID,
				fromClient.getClientId());
		toClientRegistration.setProperty(ClientRegistrationResponse.CLIENT_ID_ISSUED_AT,
				fromClient.getRegisteredAt());
		toClientRegistration.setProperty(OAuthExtensionConstants.CLIENT_NAME_I18N,
				createClientNameMap(fromClient));
		toClientRegistration.setProperty(ClientRegistrationResponse.CLIENT_SECRET,
				fromClient.getClientSecret());
		toClientRegistration.setScope(convertListOfScopesToString(fromClient.getRegisteredScopes()));
		toClientRegistration.setResponseTypes(RESPONSE_TYPE_LIST);
		
		return toClientRegistration;
	}
	
	public static ClientRegistrationResponse fill(Client fromClient,
			ClientRegistrationResponse toClientRegistrationResponse) {
		
		if (null == fromClient) {
			throw new IllegalArgumentException("fromClient is null.");
		}
		
		if (null == toClientRegistrationResponse) {
			throw new IllegalArgumentException("toClientRegistrationResponse is null.");
		}
		
		toClientRegistrationResponse.setProperty(OAuthExtensionConstants.CLIENT_DESCRIPTION,
				fromClient.getApplicationDescription());
		toClientRegistrationResponse.setProperty(ClientRegistration.CLIENT_NAME,
				fromClient.getApplicationName());
		toClientRegistrationResponse.setProperty(OAuthExtensionConstants.CLIENT_NAME_I18N,
				createClientNameMap(fromClient));
		toClientRegistrationResponse.setProperty(ClientRegistration.GRANT_TYPES,
				fromClient.getAllowedGrantTypes());
		toClientRegistrationResponse.setProperty(ClientRegistration.REDIRECT_URIS,
				fromClient.getRedirectUris());
		toClientRegistrationResponse.setProperty(ClientRegistration.RESPONSE_TYPES,
				RESPONSE_TYPE_LIST);
		toClientRegistrationResponse.setProperty(ClientRegistration.SCOPE,
				convertListOfScopesToString(fromClient.getRegisteredScopes()));
		toClientRegistrationResponse.setProperty(ClientRegistration.TOKEN_ENDPOINT_AUTH_METHOD,
				fromClient.getTokenEndpointAuthMethod());
		
		return toClientRegistrationResponse;
	}
	
	public static BearerAccessToken create(Client client, InMemoryAccessToken inMemoryAccessToken) {
		
		if (null == client) {
			throw new IllegalArgumentException("client is null.");
		}
		
		if (null == inMemoryAccessToken) {
			throw new IllegalArgumentException("inMemoryAccessToken is null.");
		}
		
		BearerAccessToken accessToken = new BearerAccessToken(
				client,
				inMemoryAccessToken.getToken().toString(),
				inMemoryAccessToken.getExpiresIn(),
				inMemoryAccessToken.getIssuedAt());
		
		accessToken.setGrantCode(inMemoryAccessToken.getAuthorizationCode());
		
		for (String scope: inMemoryAccessToken.getScopes()) {
			accessToken.getScopes().add(new OAuthPermission(scope));
		}
		
		// UserSubject
		UserSubject subject = new UserSubject();
		subject.setLogin(inMemoryAccessToken.getUsername());
		subject.getProperties().put(KEY_USER_PROPERTY, inMemoryAccessToken.getUserProperty());
		accessToken.setSubject(subject);
		
		return accessToken;
	}
	
	public static InMemoryAccessToken create(ServerAccessToken serverAccessToken) {
		
		if (null == serverAccessToken) {
			throw new IllegalArgumentException("serverAccessToken is null.");
		}
		
		InMemoryAccessToken inMemoryAccessToken = new InMemoryAccessToken();
		
		inMemoryAccessToken.setAuthorizationCode(serverAccessToken.getGrantCode());
		inMemoryAccessToken.setClientId(UUID.fromString(serverAccessToken.getClient().getClientId()));
		inMemoryAccessToken.setExpiresIn(serverAccessToken.getExpiresIn());
		inMemoryAccessToken.setIssuedAt(serverAccessToken.getIssuedAt());
		
		for (OAuthPermission permission: serverAccessToken.getScopes()) {
			inMemoryAccessToken.getScopes().add(permission.getPermission());
		}
		
		inMemoryAccessToken.setToken(serverAccessToken.getTokenKey());
		
		// UserSubject
		inMemoryAccessToken.setUsername(serverAccessToken.getSubject().getLogin());
		inMemoryAccessToken.setUserProperty(
				serverAccessToken.getSubject().getProperties().get(KEY_USER_PROPERTY));
		
		return inMemoryAccessToken;
	}
	
	public static RefreshToken create(Client client, InMemoryRefreshToken inMemoryRefreshToken) {
		
		if (null == inMemoryRefreshToken) {
			throw new IllegalArgumentException("inMemoryRefreshToken is null.");
		}
		
		if (null == client) {
			throw new IllegalArgumentException("client is null.");
		}
		
		RefreshToken refreshToken = new RefreshToken(
				client,
				inMemoryRefreshToken.getToken().toString(),
				inMemoryRefreshToken.getExpiresIn(),
				inMemoryRefreshToken.getIssuedAt());
		
		refreshToken.setGrantCode(inMemoryRefreshToken.getAuthorizationCode());
		
		for (String scope: inMemoryRefreshToken.getScopes()) {
			refreshToken.getScopes().add(new OAuthPermission(scope));
		}
		
		if (null != inMemoryRefreshToken.getAccessToken()) {
			refreshToken.getAccessTokens().add(inMemoryRefreshToken.getAccessToken().toString());
		}
		
		// UserSubject
		UserSubject subject = new UserSubject();
		subject.setLogin(inMemoryRefreshToken.getUsername());
		subject.getProperties().put(KEY_USER_PROPERTY, inMemoryRefreshToken.getUserProperty());
		refreshToken.setSubject(subject);
		
		return refreshToken;
	}
	
	public static InMemoryRefreshToken create(RefreshToken refreshToken) {
		
		if (null == refreshToken) {
			throw new IllegalArgumentException("refreshToken is null.");
		}
		
		InMemoryRefreshToken inMemoryRefreshToken = new InMemoryRefreshToken();
		
		if (!refreshToken.getAccessTokens().isEmpty()) {
			inMemoryRefreshToken.setAccessToken(refreshToken.getAccessTokens().get(0));
		}
		
		inMemoryRefreshToken.setClientId(UUID.fromString(refreshToken.getClient().getClientId()));
		inMemoryRefreshToken.setExpiresIn(refreshToken.getExpiresIn());
		inMemoryRefreshToken.setAuthorizationCode(refreshToken.getGrantCode());
		inMemoryRefreshToken.setIssuedAt(refreshToken.getIssuedAt());
		
		for (OAuthPermission permission: refreshToken.getScopes()) {
			inMemoryRefreshToken.getScopes().add(permission.getPermission());
		}
		
		inMemoryRefreshToken.setToken(refreshToken.getTokenKey());
		
		// UserSubject
		inMemoryRefreshToken.setUsername(refreshToken.getSubject().getLogin());
		inMemoryRefreshToken.setUserProperty(
				refreshToken.getSubject().getProperties().get(KEY_USER_PROPERTY));
		
		return inMemoryRefreshToken;
	}
	
	public static ClientRegistration createClientRegistration() {
		
		ClientRegistration clientRegistration = new ClientRegistration();
		
		clientRegistration.setGrantTypes(GRANT_TYPE_LIST);
		clientRegistration.setResponseTypes(RESPONSE_TYPE_LIST);
		clientRegistration.setTokenEndpointAuthMethod(OAuthConstants.TOKEN_ENDPOINT_AUTH_POST);
		
		return clientRegistration;
	}
	
	public static String convertListOfScopesToString(List<String> scopeList) {
		
		// There is a bug in "OAuthUtils.convertListOfScopesToString()" that it is not
		// space separated.
		String scopeString = "";
		
		if (null != scopeList && !scopeList.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (String scope: scopeList) {
				builder.append(scope).append(" ");
			}
			scopeString = builder.toString().trim();
		}
		
		return scopeString;
	}
	
	public static Response createResponseWithOauthError(Status status, String errorCode,
			String errorDescription) {
		
		if (null == status) {
			throw new IllegalArgumentException("status is null.");
		}
		
		if (StringUtils.isBlank(errorCode)) {
			throw new IllegalArgumentException("errorCode is blank");
		}
		
		return Response.status(status)
				.entity(new OAuthError(errorCode, errorDescription))
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
}
