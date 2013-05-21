package de.rallye.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.auth.AuthFilter;
import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Group;

@Path("groups")
public class Groups {
	
	private Logger logger =  LogManager.getLogger(Groups.class);

	private DataAdapter data = GameHandler.data;//TODO: get it _NOT_ from gameHandler (perhaps inject using Guice??)

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Group> getGroups() {
		logger.entry();
		
		try {
			List<Group> res = data.getGroups();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getGroups failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("{groupID}/avatar")
	@Produces("image/jpeg")
	public Response getGroupAvatar(@PathParam("groupID") int groupID) {
		throw new NotImplementedException();
	}
	
	@PUT
	@Path("{groupID}")
	@ResourceFilters(AuthFilter.class)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@PathParam("groupID") int groupID) {
		throw new NotImplementedException();
	}
	
	@DELETE
	@Path("{groupID}/{userID}")
	@ResourceFilters(AuthFilter.class)
	public Response logout(@PathParam("groupID") int groupID, @PathParam("userID") int userID, @Context SecurityContext sec) {
		throw new NotImplementedException();
	}
	
}
