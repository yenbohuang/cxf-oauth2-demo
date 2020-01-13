package org.yenbo.jetty.security;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.yenbo.jetty.data.PasswordInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Do not use this class since it is only for demo.
 *
 */
public class StupidPasswordEncoder implements PasswordEncoder {

	private static final Logger log = LoggerFactory.getLogger(StupidPasswordEncoder.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	@Override
	public String encode(CharSequence rawPassword) {
		// don't need this function for now.
		log.debug("Always return null");
		return null;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		
		log.debug("rawPassword={}, encodedPassword={}", rawPassword, encodedPassword);
		
		if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
			return false;
		}
		
		// carry required information and hash it in PasswordEncoder
		PasswordInfo passwordInfo = null;
		
		try {
			passwordInfo = OBJECT_MAPPER.readValue(encodedPassword, PasswordInfo.class);
		} catch (IOException ex) {
			log.error("Parsing PasswordInfo error", ex);
			return false;
		}
		
		String actual = PasswordInfo.stupidHash(
				passwordInfo.getPrefix(), (String) rawPassword, passwordInfo.getPostfix());
		String expected = passwordInfo.hash();
		
		log.debug("expected = {}, actual = {}", expected, actual);
		return expected.equals(actual);
	}

}
