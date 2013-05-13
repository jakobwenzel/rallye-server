package de.rallye.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.resource.DataHandler;
import de.rallye.rest.auth.AuthFilter;

@Path("/StadtRallye/groups")
public class Groups {
	
	private Logger logger =  LogManager.getLogger(Groups.class.getName());

	private DataHandler data = DataHandler.getInstance();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ResourceFilters(AuthFilter.class)
	public Response getGroups(@Context SecurityContext sec) {
		logger.entry();
		
		return logger.exit(data.getGroups());
	}
	
}
