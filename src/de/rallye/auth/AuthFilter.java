package de.rallye.auth;

import java.security.Principal;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import de.rallye.control.GameHandler;
import de.rallye.resource.DataHandler;

public class AuthFilter implements ResourceFilter, ContainerRequestFilter {
	
	private static Logger logger = LogManager.getLogger(AuthFilter.class);
    /**
     * Apply the filter : check input request, validate or not with user auth
     * @param containerRequest The request from Tomcat server
     */
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) throws WebApplicationException {
    	logger.entry();
    	
        String auth = containerRequest.getHeaderValue("authorization");
 
        // No Basic Auth was provided
        if(auth == null){
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
 
        String[] login = BasicAuth.decode(auth);
 
        // Not matching Basic auth conventions:  user:password
        if(login == null || login.length != 2){
        	throw new WebApplicationException(Status.UNAUTHORIZED);
        }
 
        // Checking for User
        int[] result;
		try {
			result = GameHandler.data.isAuthOk(login);
		} catch (SQLException e) {
			logger.catching(e);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
 
        // login refused
        if(result == null || result.length != 2){
        	logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        
        containerRequest.setSecurityContext(new RallyeSecurityContext(result[0], result[1]));
        logger.info("Authorized: "+ result[0] +" for group "+ result[1]);
        return logger.exit(containerRequest);
    }

	@Override
	public ContainerRequestFilter getRequestFilter() {
		return new AuthFilter();
	}

	@Override
	public ContainerResponseFilter getResponseFilter() {
		return null;
	}
}