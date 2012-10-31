package de.stadtrallye.control;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.server.impl.application.WebApplicationImpl;

import de.stadtrallye.model.DataHandler;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	static DataHandler data;
	public ClientListener() {
		//create a new static instance of the DataHandler
		data = new DataHandler();
	}
	
	
	
	/**
	 *  this method will respond to a chat event. 
	 * @param type
	 * @return
	 * @author Felix HŸbner
	 */
	@Path("chat")
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
	@Path("map")
	@GET
	@Produces({"text/plain", "application/json"})
	public String getMapEvent() {
		StringBuilder str = new StringBuilder();
		str.append("Respond to MAP-Event on host: http://"+data.getUri()+":"+data.getPort());
		return str.toString();
	}
	
	
	
	// only for debug
	@Path("{x: .*}")
	@GET
	public Response getOtherEvent() {
		return Response.status(404).entity("Unknown Event!!").build();
		//throw new WebApplicationException(404);
	}
}
