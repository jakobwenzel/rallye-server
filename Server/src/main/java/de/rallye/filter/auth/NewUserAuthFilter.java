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

package de.rallye.filter.auth;

import de.rallye.annotations.NewUserAuth;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@NewUserAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class NewUserAuthFilter extends BaseAuthFilter {
	
	private static final Logger logger = LogManager.getLogger(NewUserAuthFilter.class);
	
	@Override
	protected Response getUnauthorized() {
		return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"RallyeNewUser\"").build();
	}

	@Override
	protected GroupPrincipal checkAuthentication(ContainerRequestContext containerRequest, String[] login) {
		// Checking for User
		logger.entry();
        GroupPrincipal principal;
        int groupID;
		try {
			if (login.length != 2)
				throw new InputException("Login does not contain username and password");
			
			groupID = Integer.parseInt(login[0]);
			logger.info("Got group id: "+groupID);
			
			principal = data.getNewUserAuthorization(groupID, login[1]);
			logger.info("Got principal {}",principal);
		} catch (DataException e) {
			logger.error("Database Error", e);
			throw new WebApplicationException(e);
		} catch (NumberFormatException e) {
			logger.info(e);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		} catch (InputException e) {
			logger.error("Invalid login", e);
			throw new WebApplicationException(e);
		} catch (UnauthorizedException e) {
			logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(Status.UNAUTHORIZED);
		} catch (Exception e) {
			logger.error("Unknown Error", e);
			throw new WebApplicationException(e);
		}
        
        containerRequest.setSecurityContext(new RallyeSecurityContext<GroupPrincipal>(principal));
        logger.info("Authorized: group {}:{}", groupID, principal.getName());
        return principal;
	}
}
