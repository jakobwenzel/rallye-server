package de.rallye.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import de.rallye.annotations.KnownUserAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeResources;
import de.rallye.auth.RallyePrincipal;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.WebAppExcept;
import de.rallye.model.structures.GameState;


@Path("rallye/game")
public class Game {
	public static final String API_NAME = "scotlandYard";
	public static final int API_VERSION = 3;

	private Logger logger =  LogManager.getLogger(Game.class);

	private RallyeResources R = RallyeResources.getResources();


	@GET
	@KnownUserAuth
	@Path("state")
	@Produces(MediaType.APPLICATION_JSON)
	public GameState getChats(@Context SecurityContext sec) {
		return R.getGameState();
	}
	

	@POST
	@KnownUserAuth
	@Path("nextPosition")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setUpcomingPosition(@Context SecurityContext sec, int nodeID) {
		logger.entry();
		int groupId = ((RallyePrincipal)sec.getUserPrincipal()).getGroupID();
		
		logger.debug(groupId +" goes to "+nodeID);
		
		try {
			R.getGameState().setUpcomingPosition(groupId,nodeID);
		} catch (DataException e) {
			logger.error("getChatrooms failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} catch (WebAppExcept e) {
			logger.warn(e);
			throw e;
		}
		
		return logger.exit(Response.ok().build());
	}
	
	
}
