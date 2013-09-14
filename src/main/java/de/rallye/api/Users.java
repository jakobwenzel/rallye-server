package de.rallye.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.annotations.KnownUserOrAdminAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.GroupUser;

@Path("rallye/users")
public class Users {
	
	private final Logger logger = LogManager.getLogger(System.class);
	
	@Inject	IDataAdapter data;
	
	@GET
	@KnownUserOrAdminAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<GroupUser> getMembers() throws DataException {
		logger.entry();
		
		List<GroupUser> res = data.getAllUsers();
		return logger.exit(res);
	}
}
