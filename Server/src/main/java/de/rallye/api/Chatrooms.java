/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.api;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.*;
import de.rallye.push.PushService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("chat/rooms")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Chatrooms {
	
	private static final Logger logger =  LogManager.getLogger(Chatrooms.class);

	@Inject	IDataAdapter data;
	@Inject	PushService push;
	@Inject java.util.Map<String, ChatPictureLink> chatPictureMap;

	@GET
	@KnownUserAuth
	public List<Chatroom> getChatrooms(@Context SecurityContext sec) throws DataException{
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
	
		List<Chatroom> res = data.getChatrooms(p.getGroupID());
		return logger.exit(res);
	}
	
	@GET
	@KnownUserAuth
	@Path("{roomID}/since/{timestamp}")
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @PathParam("timestamp") long timestamp, @Context SecurityContext sec) throws DataException {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		return getChats(roomID, timestamp, p.getGroupID());
	}
	
	@GET
	@KnownUserAuth
	@Path("{roomID}")
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
	public ChatEntry addChat(@PathParam("roomID") int roomID, PostChat chat, @Context SecurityContext sec) throws DataException {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		int groupID = p.getGroupID();
		int userID = p.getUserID();
		
//		if (chat.message.isEmpty())
//			throw new WebApplicationException(new InputException("Message must not be empty"), Response.Status.BAD_REQUEST);

		if (!p.hasRightsForChatroom(roomID)) {
			logger.warn("group "+ groupID +" has no access rights for chatroom "+ roomID);
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		
		ChatEntry res;
		
		if (chat.pictureHash != null) {
			ChatPictureLink link = ChatPictureLink.getLink(chatPictureMap, chat.pictureHash, data);
			
			ImageRepository.Picture picture = link.getPicture();
			if (picture != null) {
				logger.debug("Resolved pictureHash {} to picID {}", chat.pictureHash, picture.getPictureID());
				res = data.addChat(chat, picture, roomID, groupID, userID);
			} else {
				res = data.addChat(chat, null, roomID, groupID, userID);
				link.setChat(res, roomID, push);
				logger.debug("Unresolved pictureHash");
			}
		} else
			res = data.addChat(chat, null, roomID, groupID, userID);
		
		push.chatAdded(res, roomID);
		
		return logger.exit(res);
	
	}
	
	@GET
	@Path("{roomID}/members")
	public List<? extends User> getMembers(@PathParam("roomID") int roomID) throws DataException {
		logger.entry();
		
		List<? extends User> res = data.getChatroomMembers(roomID);
		return logger.exit(res);
		
	}
	
}
