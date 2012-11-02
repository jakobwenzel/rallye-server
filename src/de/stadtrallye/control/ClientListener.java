package de.stadtrallye.control;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import de.stadtrallye.model.DataHandler;
import de.stadtrallye.model.MapHandler;
import de.stadtrallye.model.OtherHandler;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	DataHandler data = new DataHandler();
	
	//==================================================================//
	//    Map Commands
	//==================================================================//
	
	
	/*@Path("map/getNodes")
	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public JSONArray getNodes() {
		return MapHandler.getAllNodes(this.data);
	}*/
	
	@Path("map/getAllNodes")
	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public JSONArray getAllNodes() {
		//return Response.status(201).entity(MapHandler.getAllNodes(this.data).toString()).build();
		return MapHandler.getAllNodes(this.data);
	}
	
	//==================================================================//
	//    Chat Commands
	//==================================================================//
	
	@Path("chat/setNewChatEntry")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray setNewChatEntry(JSONArray chatEntry) {
		for(int i = 0; i < chatEntry.length(); i++) {
			try {
				System.out.println(chatEntry.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return chatEntry;
	}
	
	//==================================================================//
	//    Other Commands
	//==================================================================//
	
	@Path("getStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public JSONArray getStatus() {
		//return Response.status(201).entity(OtherHandler.getStatus(this.data).toString()).build();
		return OtherHandler.getStatus(this.data);
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	/**
	 *  this method will respond to a chat event. 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	/*@Path("chat/{command}")
	@GET
	@Produces({"text/plain", "application/json"})
	public Response getChatEvent(@PathParam("command") String command) {
		command = command.toLowerCase();
		
		//if (false) {
			//return this.data.map.getAllNodes();
		//} else {
		
		StringBuilder str = new StringBuilder();
		str.append("Respond to CHAT-Event");
		return Response.status(201).entity(str.toString()).build();
		//}
	}
	*/
	/**
	 *  this method will respond to a map event. 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	/*@Path("map/{command}")
	@GET
	//@Produces("application/json")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMapEvent(@PathParam("command") String command) {
		//error handling (the boolean must be an AND to be sure to stop checking if data does not exist)
		if (this.data == null) {
			throw new WebApplicationException(500);
			//return Response.status(500).entity("data does not exist!!!").build();
		}
		command = command.toLowerCase();
		
		
		if (command.equals("getallnodes")) {
			return Response.status(201).entity(MapHandler.getAllNodes(this.data).toString()).build();
		} else {
			//StringBuilder str = new StringBuilder();
			//str.append("Respond to MAP-Event on host: http://"+this.data.getUri()+":"+this.data.getPort());
			throw new WebApplicationException(400);
			//return Response.status(201).entity(str.toString()).build();
		}
	}
	*/
	
	
	// only for debug
	/*@Path("{x: .*}")
	@GET
	public Response getOtherEvent() {
		return Response.status(404).entity("Unknown Event!!").build();
		//throw new WebApplicationException(404);
	}*/
	
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
}
