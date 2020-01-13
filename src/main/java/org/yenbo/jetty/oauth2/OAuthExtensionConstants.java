package org.yenbo.jetty.oauth2;

import org.apache.cxf.rs.security.oauth2.services.ClientRegistration;

public class OAuthExtensionConstants {

	public static final String CLIENT_NAME_SPLITTER = "#";
	public static final String CLIENT_NAME_PREFIX = ClientRegistration.CLIENT_NAME + CLIENT_NAME_SPLITTER;
	public static final String INVALID_CLIENT_METADATA = "invalid_client_metadata";
	public static final String INVALID_REDIRECT_URI = "invalid_redirect_uri";
	
	// The followings are only for demo, not from any RFCs.
	public static final String CLIENT_NAME_I18N = ClientRegistration.CLIENT_NAME + "_i18n";
	public static final String CLIENT_DESCRIPTION = "client_description";
	
	private OAuthExtensionConstants() {}
}
