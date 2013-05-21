package de.rallye.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.auth.AuthFilter;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;

@ResourceFilters(AuthFilter.class)
@Path("chats")
public class Chats {

	@GET
	@ResourceFilters(AuthFilter.class)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Chatroom> getChatrooms(@Context SecurityContext sec) {
		return null;
	}
	
	@GET
	@ResourceFilters(AuthFilter.class)
	@Path("{roomID}/since/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ChatEntry> getChats(@PathParam("roomID") int roomID, @PathParam("timestamp") long timestamp, @Context SecurityContext sec) {
		return null;
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
