package de.rallye.auth;

import java.security.Principal;

public class RallyePrincipal implements Principal {
	
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
}
