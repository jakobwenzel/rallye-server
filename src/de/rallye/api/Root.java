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

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;

@Path("")
public class Root {
	private Logger logger =  LogManager.getLogger(Root.class);

	private RallyeResources R = RallyeServer.getResources();

	@GET
	public Response index(@Context SecurityContext sec) {
		
		try {
			return Response.seeOther(new URI("/client")).build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.serverError().build();
		}
		
	}
}
