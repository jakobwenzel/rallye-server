package de.rallye.auth;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.InputException;

public class GroupPrincipal implements Principal {
	
	protected Logger logger =  LogManager.getLogger(GroupPrincipal.class);
	
	protected final String name;
	protected final int groupID;

	public GroupPrincipal(int groupID, String name) {
		this.groupID = groupID;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public int getGroupID() {
		return groupID;
	}
	
	public void ensureGroupMatches(int groupID) throws WebApplicationException {
		if (this.groupID != groupID) {
			InputException e = new InputException("Authenticated Group does not match chosen group");
			logger.error(e);
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}

}
