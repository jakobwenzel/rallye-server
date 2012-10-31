package de.stadtrallye.control;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import com.sun.jersey.api.NotFoundException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	/**
	 *  this method will respond to a chat event. 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	@Path("/chat")
	@GET
	@Produces({"text/plain", "application/json"})
	public String getChatEvent() {
		StringBuilder str = new StringBuilder();
		str.append("Respond to CHAT-Event");
		return str.toString();
	}
	
	/**
	 *  this method will respond to a map event. 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	@Path("/map")
	@GET
	@Produces({"text/plain", "application/json"})
	public String getMapEvent() {
		StringBuilder str = new StringBuilder();
		str.append("Respond to MAP-Event");
		return str.toString();
	}
	
	
	@GET
	public String getOtherEvent() {
		throw new NotFoundException("Unknown Event!!");
	}
	
	/*
	public String getEvent(@PathParam("type") String type) {
		type = type.toLowerCase();
		if (type.equals("chat")) {
			StringBuilder str = new StringBuilder();
			str.append("Respond to CHAT-Event");
			return str.toString();
		} else if (type.equals("map")) {
			StringBuilder str = new StringBuilder();
			str.append("Respond to MAP-Event");
			return str.toString();
		}
		else {
			
			throw new WebApplicationException(400);
		}
	}*/
}
