package org.yenbo.jetty.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;
import org.yenbo.jetty.json.HelloJson;

@Component
@Path("/resx")
public class OAuth2ResourceService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello() {
		HelloJson json = new HelloJson();
		json.setMessage("This is an OAuth2 protected resource.");
		return Response.ok().entity(json).build();
	}
}
