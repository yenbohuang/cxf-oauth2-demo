package org.yenbo.jetty.data;

import java.io.Serializable;

public class PasswordInfo implements Serializable {

	private static final long serialVersionUID = -4576409686848091947L;
	
	private String prefix;
	private String postfix;
	private String rawPassword;
	
	public PasswordInfo(String prefix, String postfix, String rawPassword) {
		this.prefix = prefix;
		this.postfix = postfix;
		this.rawPassword = rawPassword;
	}
	
	public PasswordInfo() {}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPostfix() {
		return postfix;
	}
	
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}
	
	public String getRawPassword() {
		return rawPassword;
	}
	
	public void setRawPassword(String rawPassword) {
		this.rawPassword = rawPassword;
	}
	
	public String hash() {
		return stupidHash(prefix, rawPassword, postfix);
	}
	
	public static String stupidHash(String prefix, String rawPassword, String postfix) {
		return String.format("%s-%s-%s", prefix, rawPassword, postfix);
	}
}
