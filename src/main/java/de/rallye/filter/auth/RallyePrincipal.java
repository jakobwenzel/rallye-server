package de.rallye.filter.auth;


import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;

import de.rallye.exceptions.InputException;

public class RallyePrincipal extends GroupPrincipal {
	
	private final int userID;
	private final List<String> rights;

	public RallyePrincipal(int userID, int groupID, String name, List<String> rights) {
		super(groupID, name);
		
		this.userID = userID;
		this.rights = rights;
		
		logger = LogManager.getLogger(RallyePrincipal.class);
	}

	public int getUserID() {
		return userID;
	}
	
	public void ensureUserMatches(int userID) throws WebApplicationException {
		if (this.userID != userID) {
			InputException e = new InputException("Authenticated User does not match chosen user");
			logger.error(e);
			throw new WebApplicationException(e, Response.Status.FORBIDDEN);
		}
	}
	
	public boolean hasRightsForChatroom(int roomID) {
		return rights.contains("chatroom:"+roomID);
	}
	
	public void ensureBothMatch(int userID, int groupID) throws WebApplicationException {
		ensureGroupMatches(groupID);
		ensureUserMatches(userID);
	}
}
