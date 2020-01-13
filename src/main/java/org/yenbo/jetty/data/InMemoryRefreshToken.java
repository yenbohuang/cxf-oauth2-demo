package org.yenbo.jetty.data;

public class InMemoryRefreshToken extends AbstractInMemoryOauth2Data {

	private String token;
	private long expiresIn;
	private String authorizationCode;
	private String accessToken;
	private String username;
	private String userProperty;
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public long getExpiresIn() {
		return expiresIn;
	}
	
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
	
	public String getAuthorizationCode() {
		return authorizationCode;
	}
	
	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUserProperty() {
		return userProperty;
	}
	
	public void setUserProperty(String userProperty) {
		this.userProperty = userProperty;
	}
}
