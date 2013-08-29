package de.rallye.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.rallye.annotations.NewUserAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeResources;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;

@NewUserAuth
public class NewUserAuthFilter extends BaseAuthFilter {
	
	private final Logger logger = LogManager.getLogger(NewUserAuthFilter.class);
	
	@Override
	protected Response getUnauthorized() {
		return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"RallyeNewUser\"").build();
	}

	@Override
	protected GroupPrincipal checkAuthentication(ContainerRequestContext containerRequest, String[] login) {
		// Checking for User
        GroupPrincipal principal;
        int groupID;
		try {
			if (login.length != 2)
				throw new InputException("Login does not contain username and password");
			
			groupID = Integer.parseInt(login[0]);
			
			principal = RallyeResources.getResources().data.getNewUserAuthorization(groupID, login[1]);
		} catch (DataException e) {
			logger.error("Database Error", e);
			throw new WebApplicationException(e);
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
