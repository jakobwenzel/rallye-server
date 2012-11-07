package de.rallye.rest;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import de.rallye.model.ScottlandYardRallye;
import de.rallye.resource.ChatHandler;
import de.rallye.resource.DataHandler;
import de.rallye.resource.MapHandler;
import de.rallye.resource.OtherHandler;
import de.rallye.resource.exceptions.SQLHandlerException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	DataHandler data = new ScottlandYardRallye();

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

	@Path("map/get/nodes")
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

	@Path("chat/get")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONArray getChatEntries(JSONObject o) throws SQLHandlerException, JSONException {
		return this.data.getChatEntries(o);
	}
	
	
	
	
	@Path("chat/add/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setNewChatEntry(JSONObject o) {
		return this.data.setNewChatEntry(o);
		//BufferedImage image = BufferedImage(payload);
		
		/*try {
			InputStream is = new ByteArrayInputStream(payload);
			BufferedImage bi = ImageIO.read(is);
			Image scaled = bi.getScaledInstance(96, 96, BufferedImage.SCALE_SMOOTH);
			
			//JPEGEncoderParam param = new 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
	}

	/*@Path("chat/add/{userID}/{message}")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response setNewChatEntry(byte[] payload, @PathParam("userID") String user, @PathParam("message") String message) {
		return this.setNewChatEntryInternal(payload, user, message);
	}*/

	/**
	 * method to add a chat entry to database ( and a copy to file)
	 * @param payload the picture if available
	 * @param user userID GCM id !!!
	 * @param message message if avaliable
	 * @return a response with the http return code (and a error message)
	 * @author Felix HŸbner
	 */
	/*private Response setNewChatEntryInternal(byte[] payload, String user, String message) {
		HashSet<Integer> c;
		
		// some debug print
		System.out.println("User: " + user + " Message: '" + message + "' Payload: " + (payload.length != 0 ? "available" : "empty"));
		
		try {
			// process ChatEntry
			c = this.data.setNewChatEntry(payload, user, message);
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
	}*/
	
	// ==================================================================//
	// Picture Commands
	// ==================================================================//
	
	@Path("pic/add")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject addPicture(byte[] pic) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("picID", this.data.setPicture(pic));
		return o;
	}

	// ==================================================================//
	// Other Commands
	// ==================================================================//

	@Path("getStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getStatus() {
		// return
		// Response.status(201).entity(OtherHandler.getStatus(this.data).toString()).build();
		return OtherHandler.getStatus(this.data);
	}
	
	@Path("postPic")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject test(byte[] pic) {
		
		
		
		JSONObject o = new JSONObject();
		
		try {
			o.put("pic1", ClientListener.hexEncode(pic));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return o;
		// return
		// Response.status(201).entity(OtherHandler.getStatus(this.data).toString()).build();
		//return OtherHandler.getStatus(this.data);
	}

	// ==================================================================//
	// Helper Methods
	// ==================================================================//

	private static Appendable hexEncode(byte buf[], Appendable sb)     
	{     
	    //final Formatter formatter = new Formatter(sb);     
	    for (int i = 0; i < buf.length; i++)     
	    {     
	        int low = buf[i] & 0xF;  
	        int high = (buf[i] >> 8) & 0xF;  
	        try {
				sb.append(Character.forDigit(high, 16));
				sb.append(Character.forDigit(low, 16));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
	    }     
	    return sb;     
	}
	
	private static String hexEncode(byte buf[])  
    {  
		String s = hexEncode(buf, new StringBuilder()).toString();
		//System.out.println(s);
        return s;  
    }
	
	
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
