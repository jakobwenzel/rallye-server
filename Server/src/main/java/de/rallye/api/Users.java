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

import de.rallye.annotations.KnownUserOrAdminAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.GroupUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("users")
public class Users {
	
	private final Logger logger = LogManager.getLogger(Users.class);
	
	@Inject	IDataAdapter data;
	
	@GET
	@KnownUserOrAdminAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<GroupUser> getMembers() throws DataException {
		logger.entry();
		
		List<GroupUser> res = data.getAllUsers();
		return logger.exit(res);
	}
}
