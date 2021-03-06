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

import de.rallye.annotations.KnownUserOrAdminAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import java.security.Principal;

@KnownUserOrAdminAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class KnownUserOrAdminAuthFilter extends BaseAuthFilter{
	private static final Logger logger = LogManager.getLogger(KnownUserOrAdminAuthFilter.class);

	protected Response getUnauthorized(String message) {
		if (message!=null)
			return Response.status(Status.UNAUTHORIZED).entity(message).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RallyeAuth\"").build();
		else
			return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RallyeAuth\"").build();
	}
	
	protected Response getUnauthorized() {
		return getUnauthorized(null);
	}
	
	/*public KnownUserOrAdminAuthFilter() {
		knownUserAuth = new KnownUserAuthFilter();
		adminAuth = new AdminAuthFilter();
	}
	KnownUserAuthFilter knownUserAuth;
	AdminAuthFilter adminAuth;
	*/

	@Override
	protected Principal checkAuthentication(
			ContainerRequestContext containerRequest, String[] login) {
		if (data==null)
			logger.warn("Data is null");

		try {
			return (new KnownUserAuthFilter(data)).checkAuthentication(containerRequest, login);
		} catch (WebApplicationException e) {
			try {
				return (new AdminAuthFilter(data)).checkAuthentication(containerRequest, login);
			} catch (WebApplicationException f) {
				//Rethrow exception generated by knownUserAuth,
				throw e;
			}
		}
	}

}
