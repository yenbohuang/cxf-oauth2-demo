package org.yenbo.jetty.oauth2;

import java.util.List;
import java.util.UUID;

import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.provider.AbstractOAuthDataProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yenbo.jetty.data.InMemoryClient;
import org.yenbo.jetty.repo.AccessTokenRepository;
import org.yenbo.jetty.repo.AuthorizationCodeRepository;
import org.yenbo.jetty.repo.ClientRepository;
import org.yenbo.jetty.repo.RefreshTokenRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DynamicRegistrationOAuthDataProvider extends AbstractOAuthDataProvider {

	private static final Logger log = LoggerFactory.getLogger(
			DynamicRegistrationOAuthDataProvider.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private AccessTokenRepository accessTokenRepository;
	@Autowired
	private AuthorizationCodeRepository authorizationCodeRepository;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	
	public DynamicRegistrationOAuthDataProvider() {
		super();
	}
	
	@Override
	public ServerAccessToken getAccessToken(String accessToken) throws OAuthServiceException {
		log.warn("getAccessToken() is not implemented");
		return null;
	}

	@Override
	public List<ServerAccessToken> getAccessTokens(Client client, UserSubject subject)
			throws OAuthServiceException {
		log.warn("getAccessTokens is not implemented");
		return null;
	}

	@Override
	public List<RefreshToken> getRefreshTokens(Client client, UserSubject subject)
			throws OAuthServiceException {
		log.warn("getRefreshTokens is not implemented");
		return null;
	}

	@Override
	public void setClient(Client client) {
		
		InMemoryClient inMemoryClient = Oauth2Factory.create(client);
		clientRepository.saveOrUpdate(inMemoryClient, inMemoryClient.getClientId());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Client={}", OBJECT_MAPPER.writeValueAsString(client));
				log.debug("InMemoryClient={}", OBJECT_MAPPER.writeValueAsString(inMemoryClient));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public List<Client> getClients(UserSubject resourceOwner) {
		log.warn("getClients is not implemented");
		return null;
	}

	@Override
	protected void saveAccessToken(ServerAccessToken serverToken) {
		log.warn("saveAccessToken is not implemented");
	}

	@Override
	protected void saveRefreshToken(RefreshToken refreshToken) {
		log.warn("saveRefreshToken is not implemented");
	}

	@Override
	protected void doRevokeAccessToken(ServerAccessToken accessToken) {
		log.warn("doRevokeAccessToken is not implemented");
	}

	@Override
	protected void doRevokeRefreshToken(RefreshToken refreshToken) {
		log.warn("doRevokeRefreshToken is not implemented");
	}

	@Override
	protected RefreshToken getRefreshToken(String refreshTokenKey) {
		log.warn("getRefreshToken is not implemented");
		return null;
	}

	@Override
	protected Client doGetClient(String clientId) {
		
		UUID clietUuid = null;
		
		try {
			clietUuid = UUID.fromString(clientId);
		} catch (IllegalArgumentException ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
		
		Client client = null;
		InMemoryClient inMemoryClient = clientRepository.get(clietUuid);
		
		if (null != inMemoryClient) {
			client = Oauth2Factory.create(inMemoryClient);
			
			if (log.isDebugEnabled()) {
				try {
					log.debug("InMemoryClient={}",
							OBJECT_MAPPER.writeValueAsString(inMemoryClient));
					log.debug("Client={}",
							OBJECT_MAPPER.writeValueAsString(client));
				} catch (JsonProcessingException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		return client;
	}

	@Override
	protected void doRemoveClient(Client c) {
		
		// client is retrieved before calling doRemoveClient and it must exist in DB.
		UUID clientId = UUID.fromString(c.getClientId());
		
		if (accessTokenRepository.containsClientId(clientId)
				|| authorizationCodeRepository.containsClientId(clientId)
				|| refreshTokenRepository.containsClientId(clientId)) {
			
			// do not delete when there are still valid AC/AT/RT.
			// HTTP 403 Forbidden
			log.debug("Do not delete when there are still valid AC/AT/RT: clientId={}", clientId);
			throw ExceptionUtils.toForbiddenException(null, null);
		} else {
			clientRepository.delete(clientId);
		}
	}
}
