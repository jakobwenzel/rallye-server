package de.stadtrallye.control;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
@Path("/StadtRallye")
public class ClientListener {

	// The Java method will process HTTP GET requests
	@GET
	// The Java method will produce content identified by the MIME Media
	// type "text/plain"
	@Produces("text/plain")
	public String getClichedMessage() {
		StringBuilder str = new StringBuilder();
		str.append("Hello World");
		 str.append("  TEST TEST");
		// Return some cliched textual content
		return str.toString();
	}
}
