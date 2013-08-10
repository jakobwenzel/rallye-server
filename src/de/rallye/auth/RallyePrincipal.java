package de.rallye.auth;


import java.util.List;

import javax.ws.rs.WebApplicationException;

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
	
	public void checkUserMatches(int userID) {
		if (this.userID != userID) {
			InputException e = new InputException("Authenticated User does not match chosen user");
			logger.error(e);
			throw new WebApplicationException(e);
		}
	}
	
	public boolean hasRightsForChatroom(int roomID) {
		return rights.contains("chatroom:"+roomID);
	}
	
	public void checkBothMatch(int userID, int groupID) {
		checkGroupMatches(groupID);
		checkUserMatches(userID);
	}
}
