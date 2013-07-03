package de.rallye.api;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.multipart.file.DefaultMediaTypePredictor;

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.StadtRallye;
import de.rallye.exceptions.WebAppExcept;

@Path("client")
public class Client {
	private static final String RESOURCE_PATH = "webclient/";

	private Logger logger =  LogManager.getLogger(Client.class);

	private RallyeResources R = RallyeServer.getResources();

	@GET
	@Path("{path}")
	public Response index(@PathParam("path") String path, @Context SecurityContext sec) {
		
		if (path.contains("/"))
			throw new WebAppExcept(404, "Path invalid");
		
		logger.debug("Trying to load "+RESOURCE_PATH+path);
		
		InputStream stream = StadtRallye.class.getResourceAsStream(RESOURCE_PATH+path);
		
		if (stream==null) {

			throw new WebAppExcept(404, "Not found.");
		}
		
		MediaType m = DefaultMediaTypePredictor.CommonMediaTypes.getMediaTypeFromFileName(path);
		return Response.ok(stream,m).build();
	
	}
	
	@GET
	public Response index(@Context SecurityContext sec) {
		
		return index("index.html",sec);
		
	}
}
