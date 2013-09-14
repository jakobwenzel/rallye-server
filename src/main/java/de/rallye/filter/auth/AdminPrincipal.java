package de.rallye.filter.auth;

import java.security.Principal;
import java.util.List;

public class AdminPrincipal implements Principal {
	
//	private static final Logger logger =  LogManager.getLogger(GroupPrincipal.class);

	private final int adminID;
	private final List<String> rights;
	private final String name;

	public AdminPrincipal(int adminID, String name, List<String> rights) {
		this.adminID = adminID;
		this.rights = rights;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public int getAdminID() {
		return adminID;
	}
	
	public boolean hasRightsForTaskScoring() {
		return rights.contains("taskScoring");
	}
}
