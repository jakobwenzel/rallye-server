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

import de.rallye.annotations.KnownUserAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
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

@KnownUserAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class KnownUserAuthFilter extends BaseAuthFilter implements IManualAuthentication<RallyePrincipal> {
	private static final Logger logger = LogManager.getLogger(KnownUserAuthFilter.class);
	
	public KnownUserAuthFilter() {
		
	}
	public KnownUserAuthFilter(IDataAdapter data) {
		this.data = data;
	}
	
	protected Response getUnauthorized(String message) {
		if (message!=null)
			return Response.status(Status.UNAUTHORIZED).entity(message).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RallyeAuth\"").build();
		else
			return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"RallyeAuth\"").build();
	}
	
	protected Response getUnauthorized() {
		return getUnauthorized(null);
	}
	
	@Override
	public RallyePrincipal checkAuthentication(String[] login) {
		return checkAuthentication(null, login);
	}

    @Override
    protected RallyePrincipal checkAuthentication(ContainerRequestContext containerRequest, String[] login) {
    	// Checking for User
        RallyePrincipal principal;
        int userID, groupID;
		try {
			if (login.length != 2)
				throw new UnauthorizedException("Login does not contain username and password");
			
			String[] usr = login[0].split("@");//0: userID, 1:groupID
			
			if (usr.length!=2) 
				throw new UnauthorizedException("Username does not contain userID and groupID");
			
			userID = Integer.parseInt(usr[0]);
			groupID = Integer.parseInt(usr[1]);
			assert(data!=null);
			if (data==null)
				logger.warn("Data is null");
			principal = data.getKnownUserAuthorization(groupID, userID, login[1]);
		} catch (DataException e) {
			logger.error("Database Error", e);
			throw new WebApplicationException(e);
		} catch (InputException e) {
			logger.error("Invalid login", e);
			throw new WebApplicationException(e);
		} catch (UnauthorizedException e) {
			logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(getUnauthorized(e.getMessage()));
		} catch (Exception e) {
			logger.error("Unknown Error", e);
			throw new WebApplicationException(e);
		}
		
		if (containerRequest != null)
			containerRequest.setSecurityContext(new RallyeSecurityContext<>(principal));
		
        logger.trace("Authorized: {}:{} for group {}", userID, principal.getName(), groupID);
        return principal;
    }
}