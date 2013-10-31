package de.rallye.filter.auth;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class RallyeSecurityContext<T extends Principal> implements SecurityContext {

	private final T principal;
	
	public RallyeSecurityContext(T principal) {
		this.principal = principal;
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
