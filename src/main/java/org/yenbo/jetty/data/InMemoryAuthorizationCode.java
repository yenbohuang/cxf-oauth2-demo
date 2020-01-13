package org.yenbo.jetty.data;

public class InMemoryAuthorizationCode extends AbstractInMemoryOauth2Data {

	private String code;
	private long expiresIn;
	private String redirectUri;
	private String username;
	private String userProperty;
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public long getExpiresIn() {
		return expiresIn;
	}
	
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
		
	public String getRedirectUri() {
		return redirectUri;
	}
	
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
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
