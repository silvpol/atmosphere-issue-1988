package com.ibid_events;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Dummy resource to allow Dropwizard service to start and redirect to test file
 * Created by Swav Swiac on 02/06/2015.
 */
@Path("/")
public class DummyResource {
	@GET
	public Response goToIndex() {
		return Response.temporaryRedirect(URI.create("/assets/index.html")).build();
	}
}
