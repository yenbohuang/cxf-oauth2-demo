package org.yenbo.jetty.data;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class DemoUserDetails extends User {

	private static final long serialVersionUID = -6081929661816284907L;
	
	private InMemoryUser inMemoryUser;
	
	public DemoUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}

	public DemoUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
	}

	public InMemoryUser getInMemoryUser() {
		return inMemoryUser;
	}
	
	public void setInMemoryUser(InMemoryUser inMemoryUser) {
		this.inMemoryUser = inMemoryUser;
	}
}
