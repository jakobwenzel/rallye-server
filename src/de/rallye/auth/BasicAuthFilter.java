package de.rallye.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public abstract class BasicAuthFilter implements ResourceFilter, ContainerRequestFilter {

	private static Logger logger = LogManager.getLogger(BasicAuthFilter.class);
	
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
            throw new WebApplicationException(getUnauthorized());
        }
 
        String[] login = BasicAuth.decode(auth);
 
        // Not matching Basic auth conventions:  user:password
        if(login == null || login.length != 2){
        	throw new WebApplicationException(getUnauthorized());
        }
        
        checkAuthentication(containerRequest, login);
        
        return logger.exit(containerRequest);
    }
    
    @Override
	public ContainerResponseFilter getResponseFilter() {
		return null;
	}
    
    protected abstract Response getUnauthorized(); 
    
    protected abstract void checkAuthentication(ContainerRequest containerRequest, String[] login);
}
