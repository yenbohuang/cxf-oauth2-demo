package org.yenbo.jetty.exception;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.exception.ExceptionContext;

public class OAuth2DemoException extends ContextedRuntimeException {

	private static final long serialVersionUID = 944681436399486337L;

	public OAuth2DemoException() {
		super();
	}

	public OAuth2DemoException(String message) {
		super(message);
	}

	public OAuth2DemoException(Throwable cause) {
		super(cause);
	}

	public OAuth2DemoException(String message, Throwable cause) {
		super(message, cause);
	}

	public OAuth2DemoException(String message, Throwable cause, ExceptionContext context) {
		super(message, cause, context);
	}
}
