package de.rallye.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class RallyeSecurityContext implements SecurityContext {

	private RallyePrincipal principal;
	
	public RallyeSecurityContext(int userID, int groupID) {
		principal = new RallyePrincipal(userID, groupID);
	}

	@Override
	public String getAuthenticationScheme() {
		return "Basic Auth";
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return true;
	}

}
