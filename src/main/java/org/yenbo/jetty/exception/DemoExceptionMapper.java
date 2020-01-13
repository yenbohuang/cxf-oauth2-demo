package org.yenbo.jetty.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class DemoExceptionMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException exception) {
		return Response
				.status(exception.getResponse().getStatus())
				.entity("This is handled by DemoExceptionMapper: " + exception.getMessage())
				.type(MediaType.TEXT_PLAIN)
				.build();
	}

}
