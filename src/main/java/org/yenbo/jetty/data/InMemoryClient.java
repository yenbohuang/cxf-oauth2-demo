package org.yenbo.jetty.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryClient extends AbstractInMemoryOauth2Data {

	private String clientSecret;
	private Set<String> redirectUris = new HashSet<>();
	private String description;
	private String name;
	private Map<String, String> nameI18nMap = new HashMap<>();
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public Set<String> getRedirectUris() {
		return redirectUris;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, String> getNameI18nMap() {
		return nameI18nMap;
	}
}
