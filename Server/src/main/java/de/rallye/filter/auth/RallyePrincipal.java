/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.filter.auth;


import de.rallye.exceptions.InputException;
import org.apache.logging.log4j.LogManager;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class RallyePrincipal extends GroupPrincipal {
	
	private final int userID;
	private final List<String> rights;

	public RallyePrincipal(int userID, int groupID, String name, List<String> rights) {
		super(groupID, name);
		
		this.userID = userID;
		this.rights = rights;
		
		logger = LogManager.getLogger(RallyePrincipal.class);
	}

	public int getUserID() {
		return userID;
	}
	
	public void ensureUserMatches(int userID) throws WebApplicationException {
		if (this.userID != userID) {
			InputException e = new InputException("Authenticated User does not match chosen user");
			logger.error(e);
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}
	
	public boolean hasRightsForChatroom(int roomID) {
		return rights.contains("chatroom:"+roomID);
	}
	
	public void ensureBothMatch(int userID, int groupID) throws WebApplicationException {
		ensureGroupMatches(groupID);
		ensureUserMatches(userID);
	}
}
