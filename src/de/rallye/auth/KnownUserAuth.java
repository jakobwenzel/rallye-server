package de.rallye.auth;

import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import de.rallye.RallyeServer;

public class KnownUserAuth extends BasicAuthFilter {
	
	private static Logger logger = LogManager.getLogger(KnownUserAuth.class);

	@Override
	protected Response getUnauthorized() {
		return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"RallyeAuth\"").build();
	}
    
    @Override
    protected void checkAuthentication(ContainerRequest containerRequest, String[] login) {
    	// Checking for User
        int[] result;
		try {
			result = RallyeServer.getResources().data.isKnownUserAuthorized(login);
		} catch (SQLException e) {
			logger.error(e);
			throw new WebApplicationException(e);
		}
 
        // login refused
        if(result == null || result.length != 2){
        	logger.info("Unauthorized: "+ login[0]);
            throw new WebApplicationException(getUnauthorized());
        }
        
        containerRequest.setSecurityContext(new RallyeSecurityContext(result[0], result[1]));
        logger.info("Authorized: "+ result[0] +" for group "+ result[1]);
    }

	@Override
	public ContainerRequestFilter getRequestFilter() {
		return new KnownUserAuth();
	}
}