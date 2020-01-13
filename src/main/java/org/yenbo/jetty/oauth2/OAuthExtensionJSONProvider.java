package org.yenbo.jetty.oauth2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.json.basic.JsonMapObject;
import org.apache.cxf.jaxrs.json.basic.JsonMapObjectReaderWriter;
import org.apache.cxf.rs.security.oauth2.provider.OAuthJSONProvider;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;
import org.apache.cxf.rs.security.oauth2.services.ClientRegistrationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Provider
@Produces("application/json")
@Consumes("application/json")
public class OAuthExtensionJSONProvider extends OAuthJSONProvider {

	private static final Logger log = LoggerFactory.getLogger(OAuthExtensionJSONProvider.class);
	
	// TODO how to sort properties automatically?
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ClientRegistration.class.isAssignableFrom(type)
				|| super.isReadable(type, genericType, annotations, mediaType);
	}
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ClientRegistrationResponse.class == type
				|| ClientRegistration.class == type
				|| super.isWriteable(type, genericType, annotations, mediaType);
	}

	@Override
	public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		
		if (ClientRegistration.class.isAssignableFrom(type)) {
			return fromMapToClientRegistration(entityStream);
		} else {
			return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
		}
	}
	
	private Map<String, Object> readFromJson(InputStream is) {
		try {
			return new JsonMapObjectReaderWriter().fromJson(is);
		} catch (Exception ex) {
			throw new WebApplicationException(ex, Oauth2Factory.createResponseWithOauthError(
					Status.BAD_REQUEST, OAuthExtensionConstants.INVALID_CLIENT_METADATA,
					"Invalid request body"));
		}
	}
	
	@SuppressWarnings("unchecked")
	private ClientRegistration fromMapToClientRegistration(InputStream is) {
		
		// If we throw OAuthServiceException here, it becomes unhandled.
		// Validate the data elsewhere and ignore unknown properties.
		Map<String, Object> params = readFromJson(is);
		ClientRegistration clientRegistration = Oauth2Factory.createClientRegistration();
		Map<String, String> clientNameMap = new HashMap<>();
		
		for (Entry<String, Object> entry: params.entrySet()) {
			
			switch(entry.getKey()) {
			
			case ClientRegistration.CLIENT_NAME:
				clientRegistration.setClientName((String) entry.getValue());
				break;
				
			case ClientRegistration.REDIRECT_URIS:
				clientRegistration.setRedirectUris((List<String>) entry.getValue());
				break;
				
			case ClientRegistration.SCOPE:
				clientRegistration.setScope((String) entry.getValue());
				break;
				
			case ClientRegistrationResponse.CLIENT_ID:
				clientRegistration.setProperty(ClientRegistrationResponse.CLIENT_ID,
						entry.getValue());
				break;
				
			case ClientRegistrationResponse.CLIENT_SECRET:
				clientRegistration.setProperty(ClientRegistrationResponse.CLIENT_SECRET,
						entry.getValue());
				break;
				
			// other properties
			case OAuthExtensionConstants.CLIENT_DESCRIPTION:
				clientRegistration.setProperty(entry.getKey(), (String) entry.getValue());
				break;
				
			default:
				// client name i18n
				if (entry.getKey().startsWith(OAuthExtensionConstants.CLIENT_NAME_PREFIX)) {
					clientNameMap.put(entry.getKey(), (String) entry.getValue());
				} else {
					log.debug("Ignore value: key={}, value={}", entry.getKey(), entry.getValue());
				}
				break;
			}
		}
		
		if (!clientNameMap.isEmpty()) {
			clientRegistration.setProperty(OAuthExtensionConstants.CLIENT_NAME_I18N, clientNameMap);
		}
		
		return clientRegistration;
	}

	@Override
	public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		
		if (obj instanceof ClientRegistrationResponse) {
			writeClientRegistrationResponse((ClientRegistrationResponse) obj, entityStream);
		} else if (obj instanceof ClientRegistration) {
			writeClientRegistration((ClientRegistration) obj, entityStream);
		} else {
			super.writeTo(obj, type, genericType, annotations, mediaType, httpHeaders, entityStream);
		}
	}
	
	private void appendArrayNode(ClientRegistrationResponse obj, ObjectNode root, String key) {
		
		List<String> list = obj.getListStringProperty(key);;
		
		if (null != list) {
			root.set(key, createArrayNode(list));
		}
	}
	
	private ArrayNode createArrayNode(List<String> list) {
		
		ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
		
		for (String str: list) {
			arrayNode.add(str);
		}
		
		return arrayNode;
	}
		
	private void logIgnoredNode(JsonMapObject obj, String key, Object value) {
		
		String clientId = null;
		
		if (obj instanceof ClientRegistration) {
			clientId = ((ClientRegistration) obj).getStringProperty(
					ClientRegistrationResponse.CLIENT_ID);
		} else if (obj instanceof ClientRegistrationResponse) {
			clientId = ((ClientRegistrationResponse) obj).getClientId();
		}
		
		log.debug("Ignore value: clientId={}, key={}, value={}", clientId, key, value);
	}
	
	private void appendStringNode(JsonMapObject obj, ObjectNode root, String key) {
		
		String value = obj.getStringProperty(key);
		
		if (StringUtils.isNotBlank(value)) {
			root.put(key, value);
		} else {
			logIgnoredNode(obj, key, value);
		}
	}
	
	private void appendLongNode(JsonMapObject obj, ObjectNode root, String key) {
		
		Long value = obj.getLongProperty(key);
		
		if (null != value) {
			root.put(key, value);
		} else {
			logIgnoredNode(obj, key, value);
		}
	}
	
	private void appendClientNameMap(JsonMapObject obj, ObjectNode root) {
		
		// client_name#<language tag>
		Map<String, Object> clientNameI18nMap = obj.getMapProperty(
				OAuthExtensionConstants.CLIENT_NAME_I18N);
		
		if (null != clientNameI18nMap && !clientNameI18nMap.isEmpty()) {
			for (Entry<String, Object> entry: clientNameI18nMap.entrySet()) {
				root.put(entry.getKey(), (String) entry.getValue());
			}
		}
	}
	
	private void writeClientRegistrationResponse(ClientRegistrationResponse obj, OutputStream os)
			throws IOException {

		// TODO how to handle "client_name#<language tag>" gracefully?
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		
		root.put(ClientRegistrationResponse.CLIENT_ID, obj.getClientId());
		root.put(ClientRegistrationResponse.CLIENT_ID_ISSUED_AT, obj.getClientIdIssuedAt());
		appendStringNode(obj, root, OAuthExtensionConstants.CLIENT_DESCRIPTION);
		appendStringNode(obj, root, ClientRegistration.CLIENT_NAME);
		appendClientNameMap(obj, root);
		root.put(ClientRegistrationResponse.CLIENT_SECRET, obj.getClientSecret());
		root.put(ClientRegistrationResponse.CLIENT_SECRET_EXPIRES_AT,
				Oauth2Factory.CLIENT_SECRET_EXPIRES_AT_SECONDS);
		appendArrayNode(obj, root, ClientRegistration.GRANT_TYPES);
		appendArrayNode(obj, root, ClientRegistration.REDIRECT_URIS);
		root.put(ClientRegistrationResponse.REG_ACCESS_TOKEN, obj.getRegistrationAccessToken());
		root.put(ClientRegistrationResponse.REG_CLIENT_URI, obj.getRegistrationClientUri());
		appendArrayNode(obj, root, ClientRegistration.RESPONSE_TYPES);
		appendStringNode(obj, root, ClientRegistration.SCOPE);
		appendStringNode(obj, root, ClientRegistration.TOKEN_ENDPOINT_AUTH_METHOD);
		
		os.write(OBJECT_MAPPER.writeValueAsBytes(root));
		os.flush();
	}
	
	private void writeClientRegistration(ClientRegistration obj, OutputStream os)
			throws IOException {
		
		// TODO how to handle "client_name#<language tag>" gracefully?
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		
		appendStringNode(obj, root, ClientRegistrationResponse.CLIENT_ID);
		appendLongNode(obj, root, ClientRegistrationResponse.CLIENT_ID_ISSUED_AT);
		appendStringNode(obj, root, OAuthExtensionConstants.CLIENT_DESCRIPTION);
		root.put(ClientRegistration.CLIENT_NAME, obj.getClientName());
		appendClientNameMap(obj, root);
		appendStringNode(obj, root, ClientRegistrationResponse.CLIENT_SECRET);
		root.put(ClientRegistrationResponse.CLIENT_SECRET_EXPIRES_AT,
				Oauth2Factory.CLIENT_SECRET_EXPIRES_AT_SECONDS);
		root.set(ClientRegistration.GRANT_TYPES, createArrayNode(obj.getGrantTypes()));
		root.set(ClientRegistration.REDIRECT_URIS, createArrayNode(obj.getRedirectUris()));
		appendStringNode(obj, root, ClientRegistrationResponse.REG_ACCESS_TOKEN);
		appendStringNode(obj, root, ClientRegistrationResponse.REG_CLIENT_URI);
		root.set(ClientRegistration.RESPONSE_TYPES, createArrayNode(obj.getResponseTypes()));
		root.put(ClientRegistration.SCOPE, obj.getScope());
		root.put(ClientRegistration.TOKEN_ENDPOINT_AUTH_METHOD, obj.getTokenEndpointAuthMethod());
		
		os.write(OBJECT_MAPPER.writeValueAsBytes(root));
		os.flush();
	}
}
