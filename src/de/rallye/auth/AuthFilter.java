package de.rallye.auth;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import com.sun.istack.internal.logging.Logger;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import de.rallye.resource.DataHandler;

/**
 * Jersey HTTP Basic Auth filter
 * @author Deisss (LGPLv3)
 */
public class AuthFilter implements ResourceFilter, ContainerRequestFilter {
	
	private static Logger logger = Logger.getLogger(AuthFilter.class);
    /**
     * Apply the filter : check input request, validate or not with user auth
     * @param containerRequest The request from Tomcat server
     */
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) throws WebApplicationException {
//        //GET, POST, PUT, DELETE, ...
//        String method = containerRequest.getMethod();
//        // myresource/get/56bCA for example
//        String path = containerRequest.getPath(true);
 
        // Non-existant
//        //We do allow wadl to be retrieve
//        if(method.equals("GET") && (path.equals("application.wadl") || path.equals("application.wadl/xsd0.xsd")){
//            return containerRequest;
//        }
 
        //Get the authentification passed in HTTP headers parameters
        String auth = containerRequest.getHeaderValue("authorization");
 
        //If the user does not have the right (does not provide any HTTP Basic Auth)
        if(auth == null){
            throw new WebApplicationException(Status.UNAUTHORIZED);
//        	return containerRequest;
        }
 
        //lap : loginAndPassword
        String[] lap = BasicAuth.decode(auth);
 
        //If login or password fail
        if(lap == null || lap.length != 2){
            throw new WebApplicationException(Status.UNAUTHORIZED);
//            return containerRequest;
        }
 
        //DO YOUR DATABASE CHECK HERE (replace that line behind)...
        final boolean result = DataHandler.getInstance().isAuthOk(lap);
 
        //Our system refuses login and password
        if(result == false){
            throw new WebApplicationException(Status.UNAUTHORIZED);
//        	return containerRequest;
        }
 
        containerRequest.setSecurityContext(new SecurityContext() {
			
			@Override
			public boolean isUserInRole(String arg0) {
				return result;
			}
			
			@Override
			public boolean isSecure() {
				return false;
			}
			
			@Override
			public Principal getUserPrincipal() {
				return null;
			}
			
			@Override
			public String getAuthenticationScheme() {
				return BASIC_AUTH;
			}
		});
        
        logger.info("Authorized: "+ lap[0]);
        return containerRequest;
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