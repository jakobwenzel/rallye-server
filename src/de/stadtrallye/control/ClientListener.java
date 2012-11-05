package de.stadtrallye.control;

import java.util.HashSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import de.stadtrallye.model.ChatHandler;
import de.stadtrallye.model.DataHandler;
import de.stadtrallye.model.MapHandler;
import de.stadtrallye.model.OtherHandler;
import de.stadtrallye.resource.exceptions.SQLHandlerException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	DataHandler data = new DataHandler();

	// ==================================================================//
	// Map Commands
	// ==================================================================//

	/*
	 * @Path("map/getNodes")
	 * 
	 * @GET
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public JSONArray getNodes() {
	 * return MapHandler.getAllNodes(this.data); }
	 */

	@Path("map/getAllNodes")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getAllNodes() {
		// return
		// Response.status(201).entity(MapHandler.getAllNodes(this.data).toString()).build();
		return MapHandler.getAllNodes(this.data);
	}

	// ==================================================================//
	// Chat Commands
	// ==================================================================//

	@Path("chat/getChatEntries/{userID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getChatEntries(@PathParam("userID") String user) throws SQLHandlerException, JSONException {
		return ChatHandler.getChatEntries(this.data,user,0);
	}
	
	
	@Path("chat/getChatEntries/{userID}/{timestamp}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getChatEntries(@PathParam("userID") String user, @PathParam("timestamp") int timestamp) throws SQLHandlerException, JSONException {
		return ChatHandler.getChatEntries(this.data,user,timestamp);
	}
	
	@Path("chat/setNewChatEntry/{userID}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response setNewChatEntry(byte[] payload, @PathParam("userID") String user) {
		return this.setNewChatEntryInternal(payload, user, null);
	}

	@Path("chat/setNewChatEntry/{userID}/{message}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response setNewChatEntry(byte[] payload, @PathParam("userID") String user, @PathParam("message") String message) {
		return this.setNewChatEntryInternal(payload, user, message);
	}

	/**
	 * method to add a chat entry to database ( and a copy to file)
	 * @param payload the picture if available
	 * @param user userID GCM id !!!
	 * @param message message if avaliable
	 * @return a response with the http return code (and a error message)
	 * @author Felix HŸbner
	 */
	private Response setNewChatEntryInternal(byte[] payload, String user, String message) {
		HashSet<Integer> c;
		
		// some debug print
		System.out.println("User: " + user + " Message: '" + message + "' Payload: " + (payload.length != 0 ? "available" : "empty"));
		
		try {
			// process ChatEntry
			c = ChatHandler.setNewChatEntry(this.data, payload, user, message);
			// update devices in chatrooms
			if (!c.isEmpty()) {
				this.data.updateDevices(c);
			}
			return Response.status(201).build();

		} catch (SQLHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).build();
		}
	}

	// ==================================================================//
	// Other Commands
	// ==================================================================//

	@Path("getStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getStatus() {
		// return
		// Response.status(201).entity(OtherHandler.getStatus(this.data).toString()).build();
		return OtherHandler.getStatus(this.data);
	}

	// ==================================================================//
	// Helper Methods
	// ==================================================================//

	/**
	 * this method will respond to a chat event.
	 * 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	/*
	 * @Path("chat/{command}")
	 * 
	 * @GET
	 * 
	 * @Produces({"text/plain", "application/json"}) public Response
	 * getChatEvent(@PathParam("command") String command) { command =
	 * command.toLowerCase();
	 * 
	 * //if (false) { //return this.data.map.getAllNodes(); //} else {
	 * 
	 * StringBuilder str = new StringBuilder();
	 * str.append("Respond to CHAT-Event"); return
	 * Response.status(201).entity(str.toString()).build(); //} }
	 */
	/**
	 * this method will respond to a map event.
	 * 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	/*
	 * @Path("map/{command}")
	 * 
	 * @GET //@Produces("application/json")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * getMapEvent(@PathParam("command") String command) { //error handling (the
	 * boolean must be an AND to be sure to stop checking if data does not
	 * exist) if (this.data == null) { throw new WebApplicationException(500);
	 * //return Response.status(500).entity("data does not exist!!!").build(); }
	 * command = command.toLowerCase();
	 * 
	 * 
	 * if (command.equals("getallnodes")) { return
	 * Response.status(201).entity(MapHandler
	 * .getAllNodes(this.data).toString()).build(); } else { //StringBuilder str
	 * = new StringBuilder();
	 * //str.append("Respond to MAP-Event on host: http://"
	 * +this.data.getUri()+":"+this.data.getPort()); throw new
	 * WebApplicationException(400); //return
	 * Response.status(201).entity(str.toString()).build(); } }
	 */

	// only for debug
	/*
	 * @Path("{x: .*}")
	 * 
	 * @GET public Response getOtherEvent() { return
	 * Response.status(404).entity("Unknown Event!!").build(); //throw new
	 * WebApplicationException(404); }
	 */

	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
}
