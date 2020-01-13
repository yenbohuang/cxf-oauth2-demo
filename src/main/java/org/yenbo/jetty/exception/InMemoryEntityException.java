package org.yenbo.jetty.exception;

import org.apache.commons.lang3.exception.ExceptionContext;

public class InMemoryEntityException extends OAuth2DemoException {

	private static final long serialVersionUID = -4301695895283400446L;

	public InMemoryEntityException() {
		super();
	}

	public InMemoryEntityException(String message) {
		super(message);
	}

	public InMemoryEntityException(Throwable cause) {
		super(cause);
	}

	public InMemoryEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public InMemoryEntityException(String message, Throwable cause, ExceptionContext context) {
		super(message, cause, context);
	}
}
