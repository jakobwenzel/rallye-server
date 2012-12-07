package de.rallye.rest;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.rallye.model.ScottlandYardRallye;
import de.rallye.resource.DataHandler;
import de.rallye.resource.exceptions.SQLHandlerException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {
	
	private Logger logger =  LogManager.getLogger(ClientListener.class.getName());


	private DataHandler data = new ScottlandYardRallye();

	// ==================================================================//
	// Map Commands
	// ==================================================================//

	@Path("map/nodes")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllNodes() {
		logger.entry();
		return logger.exit(this.data.getAllNodes());
	}
	
	@Path("map/edges")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllEdges() {
		logger.entry();
		return logger.exit(this.data.getAllEdges());
	}

	// ==================================================================//
	// Chat Commands
	// ==================================================================//

	@Path("chat/get")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getChatEntries(JSONObject o) throws SQLHandlerException, JSONException {
		logger.entry();
		return logger.exit(this.data.getChatEntries(o));
	}

	@Path("chat/add")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setNewChatEntry(JSONObject o) {
		logger.entry();
		return logger.exit(this.data.setNewChatEntry(o));
	}

	// ==================================================================//
	// Picture Commands
	// ==================================================================//

	@Path("pic/add")
	@POST
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPicture(byte[] pic) throws JSONException {
		logger.entry();
		return logger.exit(this.data.setPicture(pic));
	}
	
	@Path("pic/get/{picID}/{size}")
	@GET
	@Produces("image/jpeg")
	public Response getPicture(@PathParam("picID") int picID, @PathParam("size") String size) {
		logger.entry();
		char s = size.toLowerCase().charAt(0);
		if (s == 't' || s == 's' || s == 'l') {
			return logger.exit(this.data.returnPic(picID, s));
		}
		else {
			return logger.exit(Response.status(400).entity("size is not valid").build());
		}
	}

	// ==================================================================//
	// User Commands
	// ==================================================================//

	@Path("user/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON , MediaType.TEXT_HTML})
	public Response registerUser(JSONObject req) {
		logger.entry();
		return logger.exit(this.data.registerUser(req));
	}
	
	@Path("user/unregister")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response unregisterUser(JSONObject req) {
		logger.entry();
		return logger.exit(this.data.unregisterUser(req));
	}

	// ==================================================================//
	// System Commands
	// ==================================================================//

	@Path("system/status")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(JSONObject o) {
		logger.entry();
		return logger.exit(this.data.getStatus(o));
	}

	@Path("system/config")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig() {
		logger.entry();
		return logger.exit(this.data.getConfig());
	}
	

	// ==================================================================//
	// Helper Methods
	// ==================================================================//
}
