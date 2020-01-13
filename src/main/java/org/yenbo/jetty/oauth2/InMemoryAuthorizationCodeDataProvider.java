package org.yenbo.jetty.oauth2;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthError;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.grants.code.AbstractAuthorizationCodeDataProvider;
import org.apache.cxf.rs.security.oauth2.grants.code.AuthorizationCodeRegistration;
import org.apache.cxf.rs.security.oauth2.grants.code.ServerAuthorizationCodeGrant;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.tokens.bearer.BearerAccessToken;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yenbo.jetty.data.InMemoryAccessToken;
import org.yenbo.jetty.data.InMemoryAuthorizationCode;
import org.yenbo.jetty.data.InMemoryClient;
import org.yenbo.jetty.data.InMemoryRefreshToken;
import org.yenbo.jetty.repo.AccessTokenRepository;
import org.yenbo.jetty.repo.AuthorizationCodeRepository;
import org.yenbo.jetty.repo.ClientRepository;
import org.yenbo.jetty.repo.RefreshTokenRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InMemoryAuthorizationCodeDataProvider extends AbstractAuthorizationCodeDataProvider {

	private static final Logger log = LoggerFactory.getLogger(InMemoryAuthorizationCodeDataProvider.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private AuthorizationCodeRepository authorizationCodeRepository;
	@Autowired
	private AccessTokenRepository accessTokenRepository;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	public InMemoryAuthorizationCodeDataProvider() {
		
		super();
		// always recycle refresh tokens
		setRecycleRefreshTokens(true);
    }
	
	@Override
	protected void convertSingleScopeToPermission(Client client, String scope, List<OAuthPermission> perms) {
		
		// skip preset permissionMap and compose allowed list from client definition
		if (client.getRegisteredScopes().contains(scope)) {
			perms.add(new OAuthPermission(scope));
		} else {
			String msg = String.format(
					"Unexpected scope: clientId=%s, scope=%s, registeredScopes=%s",
					client.getClientId(), scope, client.getRegisteredScopes());
			log.warn(msg);
			throw new OAuthServiceException(new OAuthError(OAuthConstants.INVALID_SCOPE, msg));
		}
	}
	
	@Override
	protected ServerAccessToken createNewAccessToken(Client client, UserSubject userSub) {
		
		// generate customized access token
		BearerAccessToken accessToken = new BearerAccessToken(client,
				"AT:" + UUID.randomUUID().toString(),
				Oauth2Factory.ACCESS_TOKEN_EXPIRED_TIME_SECONDS,
				OAuthUtils.getIssuedAt());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("BearerAccessToken={}", OBJECT_MAPPER.writeValueAsString(accessToken));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return accessToken;
	}
	
	/**
	 * Return true and make refresh token always supported.
	 */
	@Override
	protected boolean isRefreshTokenSupported(List<String> theScopes) {
		log.debug("Always generate refresh token");
		return true;
	}
	
	@Override
	protected RefreshToken doCreateNewRefreshToken(ServerAccessToken at) {
		
		// generate customized refresh token
		RefreshToken refreshToken = super.doCreateNewRefreshToken(at);
		refreshToken.setTokenKey("RT:" + UUID.randomUUID().toString());
		refreshToken.setExpiresIn(Oauth2Factory.REFRESH_TOKEN_EXPIRED_TIME_SECONDS);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("RefreshToken={}", OBJECT_MAPPER.writeValueAsString(refreshToken));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return refreshToken;
	}
	
	@Override
	protected ServerAccessToken doRefreshAccessToken(Client client, RefreshToken oldRefreshToken,
			List<String> restrictedScopes) {
		
		// always renew and grant the same scopes from old refresh token
		restrictedScopes.clear();
		ServerAccessToken accessToken = super.doRefreshAccessToken(
				client, oldRefreshToken, restrictedScopes);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Old RefreshToken={}", OBJECT_MAPPER.writeValueAsString(oldRefreshToken));
				log.debug("ServerAccessToken={}", OBJECT_MAPPER.writeValueAsString(accessToken));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return accessToken;
	}
	
	@Override
	public ServerAuthorizationCodeGrant createCodeGrant(AuthorizationCodeRegistration reg)
	        throws OAuthServiceException {
		
		ServerAuthorizationCodeGrant grant = super.createCodeGrant(reg);
		InMemoryAuthorizationCode inMemoryAuthorizationCode = Oauth2Factory.create(grant);
		authorizationCodeRepository.save(inMemoryAuthorizationCode,
				inMemoryAuthorizationCode.getCode());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("ServerAuthorizationCodeGrant={}",
						OBJECT_MAPPER.writeValueAsString(grant));
				log.debug("InMemoryAuthorizationCode={}",
						OBJECT_MAPPER.writeValueAsString(inMemoryAuthorizationCode));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return grant;
    }
	
	@Override
	public ServerAuthorizationCodeGrant removeCodeGrant(String code) throws OAuthServiceException {
		
		InMemoryAuthorizationCode inMemoryAuthorizationCode =
				authorizationCodeRepository.get(code);
		
		ServerAuthorizationCodeGrant grant = null;
		
		if (null != inMemoryAuthorizationCode) {
			InMemoryClient inMemoryClient = clientRepository.get(
					inMemoryAuthorizationCode.getClientId());
			Client client = Oauth2Factory.create(inMemoryClient);
			grant = Oauth2Factory.create(client, inMemoryAuthorizationCode);
			
			if (log.isDebugEnabled()) {
				try {
					log.debug("InMemoryAuthorizationCode={}",
							OBJECT_MAPPER.writeValueAsString(inMemoryAuthorizationCode));
					log.debug("ServerAuthorizationCodeGrant={}",
							OBJECT_MAPPER.writeValueAsString(grant));
				} catch (JsonProcessingException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		if (null != grant) {
			authorizationCodeRepository.delete(code);
			
			if (log.isDebugEnabled()) {
				try {
					log.debug("ServerAuthorizationCodeGrant={}",
							OBJECT_MAPPER.writeValueAsString(grant));;
				} catch (JsonProcessingException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		return grant;
	}

	@Override
	public List<ServerAuthorizationCodeGrant> getCodeGrants(Client c, UserSubject subject)
			throws OAuthServiceException {
		// TODO Auto-generated method stub
		log.warn("getCodeGrants is not implemented");
		return null;
	}

	@Override
	public ServerAccessToken getAccessToken(String accessToken) throws OAuthServiceException {
		
		if (StringUtils.isBlank(accessToken)) {
			throw new OAuthServiceException(new OAuthError(OAuthConstants.INVALID_GRANT,
					"access token is blank."));
		}
		
		ServerAccessToken serverAccessToken = null;
		InMemoryAccessToken inMemoryAccessToken = accessTokenRepository.get(accessToken);
		
		if (null != inMemoryAccessToken) {
			InMemoryClient inMemoryClient = clientRepository.get(inMemoryAccessToken.getClientId());
			Client client = Oauth2Factory.create(inMemoryClient);
			serverAccessToken = Oauth2Factory.create(client, inMemoryAccessToken);
			
			if (log.isDebugEnabled()) {
				try {
					log.debug("InMemoryAccessToken={}",
							OBJECT_MAPPER.writeValueAsString(inMemoryAccessToken));
					log.debug("ServerAccessToken={}",
							OBJECT_MAPPER.writeValueAsString(serverAccessToken));
				} catch (JsonProcessingException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		return serverAccessToken;
	}

	@Override
	public List<ServerAccessToken> getAccessTokens(Client client, UserSubject subject)
			throws OAuthServiceException {
		// TODO Auto-generated method stub
		log.warn("getAccessTokens is not implemented");
		return null;
	}

	@Override
	public List<RefreshToken> getRefreshTokens(Client client, UserSubject subject)
			throws OAuthServiceException {
		// TODO Auto-generated method stub
		log.warn("getRefreshTokens is not implemented");
		return null;
	}

	@Override
	public void setClient(Client client) {
		// TODO Auto-generated method stub
		log.warn("setClient is not implemented");
	}

	@Override
	public List<Client> getClients(UserSubject resourceOwner) {
		// TODO Auto-generated method stub
		log.warn("getClients is not implemented");
		return null;
	}

	@Override
	protected void saveAccessToken(ServerAccessToken serverToken) {
		
		InMemoryAccessToken inMemoryAccessToken = Oauth2Factory.create(serverToken);
		accessTokenRepository.save(inMemoryAccessToken, inMemoryAccessToken.getToken());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("ServerAccessToken={}",
						OBJECT_MAPPER.writeValueAsString(serverToken));
				log.debug("InMemoryAccessToken={}",
						OBJECT_MAPPER.writeValueAsString(inMemoryAccessToken));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	@Override
	protected void saveRefreshToken(RefreshToken refreshToken) {
		
		InMemoryRefreshToken inMemoryRefreshToken = Oauth2Factory.create(refreshToken);
		refreshTokenRepository.save(inMemoryRefreshToken, inMemoryRefreshToken.getToken());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("RefreshToken={}",
						OBJECT_MAPPER.writeValueAsString(refreshToken));
				log.debug("InMemoryRefreshToken={}",
						OBJECT_MAPPER.writeValueAsString(inMemoryRefreshToken));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	@Override
	protected void doRevokeAccessToken(ServerAccessToken accessToken) {
		
		accessTokenRepository.delete(accessToken.getTokenKey());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("ServerAccessToken={}",
						OBJECT_MAPPER.writeValueAsString(accessToken));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	@Override
	protected void doRevokeRefreshToken(RefreshToken refreshToken) {
		
		refreshTokenRepository.delete(refreshToken.getTokenKey());
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("RefreshToken={}",
						OBJECT_MAPPER.writeValueAsString(refreshToken));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		} 
	}

	@Override
	protected RefreshToken getRefreshToken(String refreshTokenKey) {
		
		if (StringUtils.isBlank(refreshTokenKey)) {
			throw new OAuthServiceException(new OAuthError(OAuthConstants.INVALID_GRANT,
					"refresh token is blank."));
		}
		
		RefreshToken refreshToken = null;
		InMemoryRefreshToken inMemoryRefreshToken = refreshTokenRepository.get(refreshTokenKey);
		
		if (null != inMemoryRefreshToken) {
			InMemoryClient inMemoryClient = clientRepository.get(inMemoryRefreshToken.getClientId());
			Client client = Oauth2Factory.create(inMemoryClient);
			refreshToken = Oauth2Factory.create(client, inMemoryRefreshToken);
			
			if (log.isDebugEnabled()) {
				try {
					log.debug("InMemoryRefreshToken={}",
							OBJECT_MAPPER.writeValueAsString(inMemoryRefreshToken));
					log.debug("RefreshToken={}",
							OBJECT_MAPPER.writeValueAsString(refreshToken));
				} catch (JsonProcessingException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		return refreshToken;
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
		// TODO Auto-generated method stub
		log.warn("doRemoveClient is not implemented");
	}

}
