package de.rallye.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.rallye.annotations.KnownUserAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeResources;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.GroupUser;

@Path("rallye/users")
public class Users {
	
	private final Logger logger = LogManager.getLogger(System.class);
	
	private RallyeResources R = RallyeResources.getResources();
	
	@GET
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<GroupUser> getMembers() {
		logger.entry();
		
		try {
			List<GroupUser> res = R.data.getAllUsers();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getAllUsers failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
