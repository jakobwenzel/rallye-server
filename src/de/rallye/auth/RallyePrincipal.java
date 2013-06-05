package de.rallye.auth;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.InputException;

public class RallyePrincipal implements Principal {
	
	private Logger logger =  LogManager.getLogger(RallyePrincipal.class);
	
	private int groupID;
	private int userID;

	RallyePrincipal(int userID, int groupID) {
		this.userID = userID;
		this.groupID = groupID;
	}

	@Override
	public String getName() {
		return "RallyePrincipal";
	}

	public int getUserID() {
		return userID;
	}
	
	public int getGroupID() {
		return groupID;
	}
	
	public void checkUserMatches(int userID) {
		if (this.userID != userID) {
			InputException e = new InputException("Authenticated User does not match chosen user");
			logger.error(e);
			throw new WebApplicationException(e);
		}
	}
	
	public void checkGroupMatches(int groupID) {
		if (this.groupID != groupID) {
			InputException e = new InputException("Authenticated Group does not match chosen group");
			logger.error(e);
			throw new WebApplicationException(e);
		}
	}
	
	public void checkBothMatch(int userID, int groupID) {
		checkGroupMatches(groupID);
		checkUserMatches(userID);
	}
}
