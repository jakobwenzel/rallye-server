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

import de.rallye.annotations.AdminAuth;
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

@AdminAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AdminAuthFilter extends BaseAuthFilter {
	
	private final Logger logger = LogManager.getLogger(AdminAuthFilter.class);


	@Override
	protected Response getUnauthorized() {
		return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"AdminAuth\"").build();
	}
	
	
	public AdminAuthFilter() {
		
	}
	public AdminAuthFilter(IDataAdapter data) {
		this.data = data;
	}

	public AdminPrincipal checkAuthentication(String[] login) {
		return checkAuthentication(null, login);
	}

	@Override
	public AdminPrincipal checkAuthentication(ContainerRequestContext containerRequest, String[] login) {

        AdminPrincipal principal;
		try {
			if (login.length != 2)
				throw new InputException("Login does not contain username and password");

			principal = data.getAdminPrincipal(login[0],login[1]);
			if (principal==null)
				throw new UnauthorizedException();
		} catch (DataException e) {
			logger.error("Database Error", e);
			throw new WebApplicationException(e);
		} catch (InputException e) {
			logger.error("Invalid login", e);
			throw new WebApplicationException(e);
		} catch (UnauthorizedException e) {
			logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(getUnauthorized());
		} catch (Exception e) {
			logger.error("Unknown Error", e);
			throw new WebApplicationException(e);
		}
        
		if (containerRequest!=null)
			containerRequest.setSecurityContext(new RallyeSecurityContext<>(principal));
        logger.trace("Authorized: {}:{}", principal.getAdminID(), principal.getName());
        return principal;
	}

}
