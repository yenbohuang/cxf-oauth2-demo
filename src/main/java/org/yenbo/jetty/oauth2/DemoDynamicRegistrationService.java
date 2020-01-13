package org.yenbo.jetty.oauth2;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.json.basic.JsonMapObject;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.provider.ClientRegistrationProvider;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistrationResponse;
import org.apache.cxf.rs.security.oauth2.services.DynamicRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yenbo.jetty.cxf.Oauth2Configuration;
import org.yenbo.jetty.data.InMemoryUser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoDynamicRegistrationService extends DynamicRegistrationService {

	private static final Logger log = LoggerFactory.getLogger(DemoDynamicRegistrationService.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private ClientRegistrationProvider clientProvider;
	
	public DemoDynamicRegistrationService() {
		super();
	}
	
	@Override
	public void setClientProvider(ClientRegistrationProvider clientProvider) {
		// workaround for accessing clientProvider
        this.clientProvider = clientProvider;
        super.setClientProvider(clientProvider);
    }
	
	@Override
	protected String generateClientId() {
		return UUID.randomUUID().toString();
	}
	
	@Override
	protected Client createNewClient(ClientRegistration request) {
		
		Client client = super.createNewClient(request);
		Oauth2Factory.fill(request, client, true);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("ClientRegistration={}", OBJECT_MAPPER.writeValueAsString(request));
				log.debug("Client={}", OBJECT_MAPPER.writeValueAsString(client));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return client;
	}
	
	@Override
	protected ClientRegistrationResponse fromClientToRegistrationResponse(Client client) {
		return fromClientToRegistrationResponse(client, false);
	}
	
	private void setRegistrationAccessTokenAndUri(JsonMapObject jsonMapObject,
			String clientId, boolean isUpdate) {
		
		// both registration access token and uri are either included or excluded
		if (Oauth2Configuration.SUPPORT_REGISTRATION_ACCESS_TOKEN) {
            
        	UriBuilder ub = getMessageContext().getUriInfo().getAbsolutePathBuilder();
        	
        	if (isUpdate) {
        		jsonMapObject.setProperty(ClientRegistrationResponse.REG_CLIENT_URI,
	        			ub.build().toString());
        	} else {
        		jsonMapObject.setProperty(ClientRegistrationResponse.REG_CLIENT_URI,
	        			ub.path(clientId).build().toString());
        	}
        	
        	jsonMapObject.setProperty(ClientRegistrationResponse.REG_ACCESS_TOKEN,
    				getRequestAccessToken());
        }
	}
	
	private ClientRegistrationResponse fromClientToRegistrationResponse(Client client,
			boolean isUpdate) {
		
		ClientRegistrationResponse response = super.fromClientToRegistrationResponse(client);
		setRegistrationAccessTokenAndUri(response, client.getClientId(), isUpdate);
		Oauth2Factory.fill(client, response);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Client={}", OBJECT_MAPPER.writeValueAsString(client));
				log.debug("ClientRegistrationResponse={}", OBJECT_MAPPER.writeValueAsString(response));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return response;
	}
	
	@Override
	protected ClientRegistration fromClientToClientRegistration(Client client) {
		
		ClientRegistration clientRegistration = super.fromClientToClientRegistration(client);
        setRegistrationAccessTokenAndUri(clientRegistration, client.getClientId(), true);
		Oauth2Factory.fill(client, clientRegistration);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Client={}", OBJECT_MAPPER.writeValueAsString(client));
				log.debug("ClientRegistrationResponse={}", OBJECT_MAPPER.writeValueAsString(
						clientRegistration));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return clientRegistration;
	}
	
	@Override
	protected void checkInitialAuthentication() {
		
		String accessToken = getRequestAccessToken();
        if (!InMemoryUser.ACCESS_TOKEN_ADMIN.equals(accessToken)) {
        	log.debug("Initial access token not match: {}", accessToken);
            throw ExceptionUtils.toNotAuthorizedException(null, null);
        } else {
        	log.debug("Initial access token matched.");
        }
	}
	
	@Override
	protected Client readClient(String clientId) {
		
        String accessToken = getRequestAccessToken();
        checkRegistrationAccessToken(accessToken);
                                                 
        Client c = clientProvider.getClient(clientId);
        if (c == null) {
            throw ExceptionUtils.toNotAuthorizedException(null, null);
        }
        
        return c;
    }
	
	@Override
	protected String createRegAccessToken(Client client) {
		
		String accessToken = getRequestAccessToken();
		client.getProperties().put(ClientRegistrationResponse.REG_ACCESS_TOKEN, accessToken);
		return accessToken;
	}
	
	private void checkRegistrationAccessToken(String accessToken) {
		
		// only check if access token belongs to admin user
		if (!InMemoryUser.ACCESS_TOKEN_ADMIN.equals(accessToken)) {
			log.debug("registration access token not match: expected={}, actual={}",
					InMemoryUser.ACCESS_TOKEN_ADMIN, accessToken);
			throw ExceptionUtils.toNotAuthorizedException(null, null);
		} else {
			log.debug("registration access token matched.");
		}
	}
	
	@Override
	@PUT
    @Path("{clientId}")
    @Consumes("application/json")
	public Response updateClientRegistration(
    		@PathParam("clientId") String clientId) {
		
		return Oauth2Factory.createResponseWithOauthError(Status.BAD_REQUEST,
				OAuthExtensionConstants.INVALID_CLIENT_METADATA, "Empty request body");
	}
	
	
	@PUT
    @Path("{clientId}")
    @Consumes("application/json")
	@Produces("application/json")
    public Response updateClientRegistration(
    		@PathParam("clientId") String clientId,
    		ClientRegistration request) {
		
		// compare client_id
		String clientIdFromRequestBody = request.getStringProperty(
				ClientRegistrationResponse.CLIENT_ID);
		
		if (StringUtils.isBlank(clientIdFromRequestBody)) {
			return Oauth2Factory.createResponseWithOauthError(
					Status.BAD_REQUEST, OAuthExtensionConstants.INVALID_CLIENT_METADATA,
					"client_id is not provided in request body.");
		}
		
		if (!clientId.equals(clientIdFromRequestBody)) {
			
			return Oauth2Factory.createResponseWithOauthError(
					Status.BAD_REQUEST, OAuthExtensionConstants.INVALID_CLIENT_METADATA,
					new StringBuilder()
						.append("client_id not match in request body and path parameter: ")
						.append("in path=").append(clientId)
						.append(", in request body=").append(clientIdFromRequestBody)
						.toString()
					);
		}
		
		Client client = readClient(clientId);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Client before update={}", OBJECT_MAPPER.writeValueAsString(client));
				log.debug("ClientRegistration={}", OBJECT_MAPPER.writeValueAsString(request));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		Oauth2Factory.fill(request, client, false);
		clientProvider.setClient(client);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("Client after update={}", OBJECT_MAPPER.writeValueAsString(client));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return Response.status(Status.OK).entity(fromClientToRegistrationResponse(client, true)).build();
    }
	
	@DELETE
    @Path("{clientId}")
    public Response deleteClientRegistration(@PathParam("clientId") String clientId) {
		
        if (readClient(clientId) != null) {
            clientProvider.removeClient(clientId);    
        }
        
        // HTTP 204 No Content
        return Response.noContent().build();
    }
}
