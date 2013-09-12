package de.rallye.api;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.GroupUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("rallye/users")
public class Users {
	
	private final Logger logger = LogManager.getLogger(System.class);
	
	@Inject	DataAdapter data;
	
	@GET
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<GroupUser> getMembers() {
		logger.entry();
		
		try {
			List<GroupUser> res = data.getAllUsers();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getAllUsers failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
