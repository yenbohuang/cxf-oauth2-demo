package org.yenbo.jetty.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.yenbo.jetty.json.HelloJson;

@Component
@Path("/demo")
public class DemoService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello() {
		HelloJson json = new HelloJson();
		json.setMessage("I am a public information.");
		return Response.ok().entity(json).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/error")
	public Response error() {
		return Response.serverError().entity("This is handled by ErrorPageErrorHandler").build();
	}
}
