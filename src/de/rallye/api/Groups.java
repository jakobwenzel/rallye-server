package de.rallye.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import de.rallye.auth.NewUserAuthFilter;
import de.rallye.auth.RallyePrincipal;
import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.LoginInfo;
import de.rallye.model.structures.PushConfig;
import de.rallye.model.structures.User;
import de.rallye.model.structures.UserAuth;

@Path("rallye/groups")
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
	@Path("{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getMembers(@PathParam("groupID") int groupID) {
		logger.entry();
		
		try {
			List<User> res = data.getMembers(groupID);
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
	
	@GET
	@Path("{groupID}/{userID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo(@PathParam("groupID") int groupID, @PathParam("userID") int userID) {
		throw new NotImplementedException();
	}
	
	@GET
	@Path("{groupID}/{userID}/pushSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public PushConfig getPushSettings(@PathParam("groupID") int groupID, @PathParam("userID") int userID, @Context SecurityContext sec) {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.checkBothMatch(userID, groupID);
		
		try {
			PushConfig res = data.getPushConfig(groupID, userID);
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("logout failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@POST
	@Path("{groupID}/{userID}/pushSettings")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setPushConfig(@PathParam("groupID") int groupID, @PathParam("userID") int userID, PushConfig push, @Context SecurityContext sec) {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.checkBothMatch(userID, groupID);
		
		try {
			data.setPushConfig(groupID, userID, push);
			return Response.ok().build();
		} catch (DataException e) {
			logger.error("logout failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PUT
	@Path("{groupID}")
	@ResourceFilters(NewUserAuthFilter.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserAuth login(@PathParam("groupID") int groupID, LoginInfo info, @Context SecurityContext sec) {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		int authGroup = p.getGroupID();
		
		p.checkGroupMatches(groupID);
		
		try {
			UserAuth login = data.login(authGroup, info);
			return login;
		} catch (DataException e) {
			logger.error("login failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} catch (InputException e) {
			logger.catching(e);
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		}
		
	}
	
	@DELETE
	@Path("{groupID}/{userID}")
	@ResourceFilters(AuthFilter.class)
	public Response logout(@PathParam("groupID") int groupID, @PathParam("userID") int userID, @Context SecurityContext sec) {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.checkBothMatch(userID, groupID);
		
		try {
			data.logout(groupID, userID);
			return Response.ok().build();
		} catch (DataException e) {
			logger.error("logout failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
}
