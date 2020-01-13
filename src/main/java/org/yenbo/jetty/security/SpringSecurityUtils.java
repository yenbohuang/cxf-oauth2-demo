package org.yenbo.jetty.security;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class SpringSecurityUtils {

	private SpringSecurityUtils() {
	}

	public static SavedRequest getSavedRequest(MessageContext messageContext) {

		if (null == messageContext) {
			throw new IllegalArgumentException("messageContext is null");
		}

		return new HttpSessionRequestCache().getRequest(messageContext.getHttpServletRequest(),
				messageContext.getHttpServletResponse());
	}
}
