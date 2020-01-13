package org.yenbo.jetty.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.yenbo.jetty.json.HelloJson;

@Component
@Path("/secret")
public class SecretService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello() {
		HelloJson json = new HelloJson();
		json.setMessage("I am protected.");
		return Response.ok().entity(json).build();
	}
}
