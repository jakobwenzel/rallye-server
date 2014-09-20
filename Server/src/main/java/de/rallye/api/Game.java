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

import de.rallye.annotations.AdminAuth;
import de.rallye.annotations.KnownUserOrAdminAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.RallyeGameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


@Path("games")
public class Game {
	public static final String API_NAME = "scotlandYard";
	public static final int API_VERSION = 3;

	private static final Logger logger =  LogManager.getLogger(Game.class);

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
