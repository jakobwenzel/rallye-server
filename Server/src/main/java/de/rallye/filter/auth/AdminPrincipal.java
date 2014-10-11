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

import java.security.Principal;
import java.util.List;

public class AdminPrincipal implements Principal {
	
//	private static final Logger logger =  LogManager.getLogger(GroupPrincipal.class);

	private final int adminID;
	private final List<String> rights;
	private final String name;

	public AdminPrincipal(int adminID, String name, List<String> rights) {
		this.adminID = adminID;
		this.rights = rights;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public int getAdminID() {
		return adminID;
	}
	
	public boolean hasRightsForTaskScoring() {
		return rights.contains("taskScoring");
	}
}
