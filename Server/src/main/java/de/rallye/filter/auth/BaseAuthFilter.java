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

import de.rallye.db.IDataAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.security.Principal;

public abstract class BaseAuthFilter implements ContainerRequestFilter {

	private final Logger logger = LogManager.getLogger(BaseAuthFilter.class);

	@Inject
	protected IDataAdapter data;
	
    /**
     * Apply the filter : check input request, validate or not with user auth
     * @param containerRequest The request from Tomcat server
     */
    @Override
    public void filter(ContainerRequestContext containerRequest) throws WebApplicationException {
    	logger.entry();
//    	logger.info("We are a {}",this.getClass().getName());
//    	logger.info(containerRequest.getUriInfo().getRequestUri());

        String auth = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);
 
        // No Basic Auth was provided
        if(auth == null){
        	logger.debug("No auth at all");
            throw new WebApplicationException(getUnauthorized());
        }
 
        String[] login = BasicAuth.decode(auth);
 
        // Not matching Basic auth conventions:  user:password
        if(login == null || login.length != 2){
        	logger.warn("Auth invalid");
        	throw new WebApplicationException(getUnauthorized());
        }
        
        checkAuthentication(containerRequest, login);
        
//        return logger.exit(containerRequest);
//        return containerRequest;
		logger.exit();
    }
    
    protected abstract Response getUnauthorized(); 
    
    protected abstract Principal checkAuthentication(ContainerRequestContext containerRequest, String[] login);
}
