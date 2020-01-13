package org.yenbo.jetty.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AbstractInMemoryOauth2Data {

	private UUID clientId;
	private Set<String> scopes = new HashSet<>();
	private long issuedAt;
	
	public UUID getClientId() {
		return clientId;
	}
	
	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}
	
	public Set<String> getScopes() {
		return scopes;
	}
	
	public long getIssuedAt() {
		return issuedAt;
	}
	
	public void setIssuedAt(long issuedAt) {
		this.issuedAt = issuedAt;
	}
}
