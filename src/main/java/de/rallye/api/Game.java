package de.rallye.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.annotations.AdminAuth;
import de.rallye.annotations.KnownUserAuth;
import de.rallye.annotations.KnownUserOrAdminAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.GameState;
import de.rallye.model.structures.RallyeGameState;


@Path("rallye/game")
public class Game {
	public static final String API_NAME = "scotlandYard";
	public static final int API_VERSION = 3;

	private Logger logger =  LogManager.getLogger(Game.class);

	@Inject RallyeGameState gameState;
	@Inject
	IDataAdapter data;


	@GET
	@KnownUserOrAdminAuth
	@Path("state")
	@Produces(MediaType.APPLICATION_JSON)
	public RallyeGameState getGameState(@Context SecurityContext sec) {
		logger.entry();
		return logger.exit(gameState);
	}


	@POST
	@AdminAuth
	@Path("state")
	public Response setGameState(RallyeGameState state) throws DataException {
		logger.entry();
		this.gameState.copy(state);
		data.saveGameState(state);

		return logger.exit(Response.ok().build());
	}

//	@POST
//	@KnownUserAuth
//	@Path("nextPosition")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response setUpcomingPosition(@Context SecurityContext sec, int nodeID) throws DataException {
//		logger.entry();
//		int groupId = ((RallyePrincipal)sec.getUserPrincipal()).getGroupID();
//
//		logger.debug(groupId +" goes to "+nodeID);
//
//		gameState.setUpcomingPosition(groupId, nodeID);
//
//		return logger.exit(Response.ok().build());
//	}
	
	
}
