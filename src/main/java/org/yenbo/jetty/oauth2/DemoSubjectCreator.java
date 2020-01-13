package org.yenbo.jetty.oauth2;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oauth2.provider.DefaultSubjectCreator;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.yenbo.jetty.data.DemoUserDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoSubjectCreator extends DefaultSubjectCreator {

	private static final Logger log = LoggerFactory.getLogger(DemoSubjectCreator.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public DemoSubjectCreator() {
		super();
	}
	
	private DemoUserDetails getUserDetail(MessageContext mc) {
		
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)
				mc.getSecurityContext().getUserPrincipal();
		DemoUserDetails userDetails = (DemoUserDetails) token.getPrincipal();
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("DemoUserDetails={}", OBJECT_MAPPER.writeValueAsString(userDetails));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		return userDetails;
	}
	
	@Override
	public UserSubject createUserSubject(MessageContext mc, MultivaluedMap<String, String> params)
			throws OAuthServiceException {
		
		UserSubject subject = super.createUserSubject(mc, params);
		DemoUserDetails userDetails = getUserDetail(mc);
		
		subject.getProperties().put(
				Oauth2Factory.KEY_USER_PROPERTY, userDetails.getInMemoryUser().getProperty());
		
		// user roles
		List<String> roles = new ArrayList<>();
		for (GrantedAuthority authority: userDetails.getAuthorities()) {
			roles.add(authority.getAuthority());
		}
		subject.setRoles(roles);
		
		if (log.isDebugEnabled()) {
			try {
				log.debug("UserSubject={}", OBJECT_MAPPER.writeValueAsString(subject));
			} catch (JsonProcessingException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		return subject;
	}

}
