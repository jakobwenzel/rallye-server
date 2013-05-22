package de.rallye.api;

import java.util.List;

import javax.ws.rs.Consumes;
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

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.auth.AuthFilter;
import de.rallye.auth.RallyePrincipal;
import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;

@ResourceFilters(AuthFilter.class)
@Path("rallye/chatrooms")
public class Chatrooms {
	
	private Logger logger =  LogManager.getLogger(Chatrooms.class);

	private DataAdapter data = GameHandler.data;//TODO: get it _NOT_ from gameHandler (perhaps inject using Guice??)

	@GET
	@ResourceFilters(AuthFilter.class)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Chatroom> getChatrooms(@Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		try {
			List<Chatroom> res = data.getChatrooms(p.getGroupID());
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getChatrooms failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@ResourceFilters(AuthFilter.class)
	@Path("{roomID}/since/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @PathParam("timestamp") long timestamp, @Context SecurityContext sec) {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		return getChats(roomID, timestamp, p.getGroupID());
	}
	
	@GET
	@ResourceFilters(AuthFilter.class)
	@Path("{roomID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @Context SecurityContext sec) {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		return getChats(roomID, 0, p.getGroupID());
	}
	
	private List<ChatEntry> getChats(int roomID, long timestamp, int groupID) {
		logger.entry();
		
		try {
			if (!data.hasRightsForChatroom(groupID, roomID)) {
				logger.warn("group "+ groupID +" has no access rights for chatroom "+ roomID);
			}
			
			List<ChatEntry> res = data.getChats(roomID, timestamp, groupID);
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getChats failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PUT
	@ResourceFilters(AuthFilter.class)
	@Path("{roomID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ChatEntry addChat(@Context SecurityContext sec) {
		return null;
	}
	
	@PUT
	@ResourceFilters(AuthFilter.class)
	@Path("{roomID}/{hash}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ChatEntry addChatWithHash(@Context SecurityContext sec, @PathParam("hash") String hash) {
		return null;
	}
	
}
