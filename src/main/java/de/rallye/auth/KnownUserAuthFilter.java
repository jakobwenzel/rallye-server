package de.rallye.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.exceptions.WebAppExcept;

@KnownUserAuth
@Provider
public class KnownUserAuthFilter extends BaseAuthFilter implements IManualAuthentication<RallyePrincipal> {
	private static Logger logger = LogManager.getLogger(KnownUserAuthFilter.class);
	
	public KnownUserAuthFilter() {
		
	}
	public KnownUserAuthFilter(IDataAdapter data) {
		this.data = data;
	}
	
	protected Response getUnauthorized(String message) {
		if (message!=null)
			return Response.status(Status.UNAUTHORIZED).entity(message).header("WWW-Authenticate", "Basic realm=\"RallyeAuth\"").build();
		else
			return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"RallyeAuth\"").build();
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
			containerRequest.setSecurityContext(new RallyeSecurityContext<RallyePrincipal>(principal));
		
        logger.info("Authorized: {}:{} for group {}", userID, principal.getName(), groupID);
        return principal;
    }
}