package de.rallye.auth;

import java.security.Principal;

public interface IManualAuthentication<T extends Principal> {
	
	T checkAuthentication(String[] login);

}
