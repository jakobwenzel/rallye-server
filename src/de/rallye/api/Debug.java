package de.rallye.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.UserInternal;

@Path("rallye/debug")
public class Debug {
	
	private Logger logger =  LogManager.getLogger(Debug.class);

	private RallyeResources R = RallyeServer.getResources();
	
	@GET
	@Path("members/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getMembers(@PathParam("groupID") int groupID) {
		try {
			return R.data.getMembers(groupID);
		} catch (DataException e) {
			logger.error("Failed to get members", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("chatroomMembers/{roomID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getChatroomMembers(@PathParam("roomID") int roomID) {
		try {
			return R.data.getChatroomMembers(roomID);
		} catch (DataException e) {
			logger.error("Failed to get chatroom members", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

}
