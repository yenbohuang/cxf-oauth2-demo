package org.yenbo.jetty.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class InMemoryUser implements Serializable {

	private static final long serialVersionUID = -2596623224404349750L;
	
	public static final String ACCESS_TOKEN_ADMIN = "AT:6a73a7ea-676a-47ae-99c3-ce9fb41b3972";
	public static final String PASSWORD_PREFIX = "abcd";
	public static final String PASSWORD_POSTFIX = "1234";
	public static final String USERNAME_ADMIN = "yenbo";
	public static final String USERNAME_USER = "test";
	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_USER = "USER";
	
	private String username;
	private String property;
	private PasswordInfo passwordInfo;
	private Set<String> roles = new HashSet<>();
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public PasswordInfo getPasswordInfo() {
		return passwordInfo;
	}
	
	public void setPasswordInfo(PasswordInfo passwordInfo) {
		this.passwordInfo = passwordInfo;
	}
	
	public String getProperty() {
		return property;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public Set<String> getRoles() {
		return roles;
	}
}
