package de.rallye.filter.auth;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import de.rallye.annotations.AdminAuth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;

@AdminAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AdminAuthFilter extends BaseAuthFilter {
	
	private final Logger logger = LogManager.getLogger(AdminAuthFilter.class);


	@Override
	protected Response getUnauthorized() {
		return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"AdminAuth\"").build();
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
			
			if (login[0].equals("taskScorer") && login[1].equals("test")) {
				List<String> rights = new ArrayList<String>();
				rights.add("taskScoring");
				principal = new AdminPrincipal(1, "taskScorer", rights);
			} else if (login[0].equals("ray") && login[1].equals("test")) {
				principal = new AdminPrincipal(2, "ray", new ArrayList<String>());
			} else {
				throw new UnauthorizedException();
			}
//		} catch (DataException e) {
//			logger.error("Database Error", e);
//			throw new WebApplicationException(e);
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
			containerRequest.setSecurityContext(new RallyeSecurityContext<AdminPrincipal>(principal));
        logger.info("Authorized: {}:{}", principal.getAdminID(), principal.getName());
        return principal;
	}

}
