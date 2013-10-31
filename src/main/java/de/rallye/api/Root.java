package de.rallye.api;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("")
public class Root {

	private static final Logger logger =  LogManager.getLogger(Root.class);

	@GET
	public Response index(@Context SecurityContext sec) {
		logger.entry();
		
		try {
			return Response.seeOther(new URI("/client")).build();
		} catch (URISyntaxException e) {
			logger.catching(e);
			return Response.serverError().build();
		}
		
	}
}
