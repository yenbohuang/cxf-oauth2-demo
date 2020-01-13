package org.yenbo.jetty.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.yenbo.jetty.data.DemoUserDetails;
import org.yenbo.jetty.data.InMemoryUser;
import org.yenbo.jetty.repo.UserRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoUserDetailsService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(DemoUserDetailsService.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		InMemoryUser inMemoryUser = userRepository.get(username);
		
		if (null == inMemoryUser) {
			throw new UsernameNotFoundException("Username not found: " + username);
		}
		
		// carry required information and hash it in PasswordEncoder
		String json = null;
		
		try {
			json = OBJECT_MAPPER.writeValueAsString(inMemoryUser.getPasswordInfo());
		} catch (JsonProcessingException ex) {
			throw new UsernameNotFoundException("Parsing json error", ex);
		}
		
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String role: inMemoryUser.getRoles()) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		
		DemoUserDetails user = new DemoUserDetails(username, json, authorities);
		user.setInMemoryUser(inMemoryUser);
		
		try {
			// Do not log password in production environment. This is only for demo.
			log.debug("{}", OBJECT_MAPPER.writeValueAsString(user));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}
		
		return user;
	}

}
