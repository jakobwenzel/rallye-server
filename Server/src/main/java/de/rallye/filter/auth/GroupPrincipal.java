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
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.filter.auth;

import de.rallye.exceptions.InputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;

public class GroupPrincipal implements Principal {
	
	protected Logger logger =  LogManager.getLogger(GroupPrincipal.class);
	
	protected final String name;
	protected final int groupID;

	public GroupPrincipal(int groupID, String name) {
		this.groupID = groupID;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public int getGroupID() {
		return groupID;
	}
	
	public void ensureGroupMatches(int groupID) throws WebApplicationException {
		if (this.groupID != groupID) {
			InputException e = new InputException("Authenticated Group does not match chosen group");
			logger.error(e);
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}

}
