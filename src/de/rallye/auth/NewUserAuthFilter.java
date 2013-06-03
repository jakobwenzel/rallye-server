package de.rallye.auth;

import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import de.rallye.control.GameHandler;

public class NewUserAuthFilter extends AuthFilter implements ResourceFilter {
	
	private static Logger logger = LogManager.getLogger(NewUserAuthFilter.class);
	
	public NewUserAuthFilter() {
		unauthorized = Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"RallyeNewUser\"").build();
	}

	@Override
	protected void checkAuthenticaion(ContainerRequest containerRequest, String[] login) {
		// Checking for User
        int result;
		try {
			result = GameHandler.data.isNewUserAuthorized(login);
		} catch (SQLException e) {
			logger.error(e);
			throw new WebApplicationException(e);
		}
 
        // login refused
        if(result <= 0){
        	logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        
        containerRequest.setSecurityContext(new RallyeSecurityContext(-1, result));
        logger.info("Authorized: group "+ result);
	}
	
	@Override
	public ContainerRequestFilter getRequestFilter() {
		return new NewUserAuthFilter();
	}
}
