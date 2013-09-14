package de.rallye.api;

import java.util.List;

import javax.inject.Inject;
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

import de.rallye.annotations.KnownUserAuth;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.Chatroom;
import de.rallye.model.structures.SimpleChatWithPictureHash;
import de.rallye.model.structures.User;
import de.rallye.push.PushService;

@Path("rallye/chatrooms")
public class Chatrooms {
	
	private static Logger logger =  LogManager.getLogger(Chatrooms.class);

	@Inject	IDataAdapter data;
	@Inject	PushService push;
	@Inject java.util.Map<String, ChatPictureLink> chatPictureMap;

	@GET
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<Chatroom> getChatrooms(@Context SecurityContext sec) throws DataException{
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
	
		List<Chatroom> res = data.getChatrooms(p.getGroupID());
		return logger.exit(res);
	}
	
	@GET
	@KnownUserAuth
	@Path("{roomID}/since/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @PathParam("timestamp") long timestamp, @Context SecurityContext sec) throws DataException {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		return getChats(roomID, timestamp, p.getGroupID());
	}
	
	@GET
	@KnownUserAuth
	@Path("{roomID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @Context SecurityContext sec) throws DataException {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		return getChats(roomID, 0, p.getGroupID());
	}
	
	private List<ChatEntry> getChats(int roomID, long timestamp, int groupID) throws DataException {
		logger.entry();
		
		try {
			List<ChatEntry> res = data.getChats(roomID, timestamp, groupID);
			return logger.exit(res);
		} catch (UnauthorizedException e) {
			logger.error("group {} has no access rights for chatroom {}", groupID, roomID);
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}
	
	@PUT
	@KnownUserAuth
	@Path("{roomID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ChatEntry addChat(@PathParam("roomID") int roomID, SimpleChatWithPictureHash chat, @Context SecurityContext sec) throws DataException {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		int groupID = p.getGroupID();
		int userID = p.getUserID();
		
		if (chat.message.isEmpty())
			throw new WebApplicationException(new InputException("Message must not be empty"), Response.Status.BAD_REQUEST);

		if (!p.hasRightsForChatroom(roomID)) {
			logger.warn("group "+ groupID +" has no access rights for chatroom "+ roomID);
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		
		ChatEntry res;
		
		if (chat.pictureHash != null) {
			ChatPictureLink link = ChatPictureLink.getLink(chatPictureMap, chat.pictureHash, data);
			
			Integer picID = link.getPictureID();
			if (picID != null) {
				chat.pictureID = picID;
				res = data.addChat(chat, roomID, groupID, userID);
			} else {
				res = data.addChat(chat, roomID, groupID, userID);
				link.setChat(res.chatID);
			}
		} else
			res = data.addChat(chat, roomID, groupID, userID);
		
		push.chatAdded(res, roomID);
		
		return logger.exit(res);
	
	}
	
	@GET
	@Path("{roomID}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public List<? extends User> getMembers(@PathParam("roomID") int roomID) throws DataException {
		logger.entry();
		
		List<? extends User> res = data.getChatroomMembers(roomID);
		return logger.exit(res);
		
	}
	
}
