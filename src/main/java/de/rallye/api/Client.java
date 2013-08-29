package de.rallye.api;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.StadtRallye;
import de.rallye.exceptions.WebAppExcept;
import org.glassfish.jersey.media.multipart.file.DefaultMediaTypePredictor;
import org.glassfish.jersey.message.internal.MediaTypeProvider;

@Path("client")
public class Client {
	private static final String RESOURCE_PATH = "webclient/";

	private Logger logger =  LogManager.getLogger(Client.class);

//	private RallyeResources R = RallyeServer.getResources();

	@GET
	@Path("{path}")
	public Response index(@PathParam("path") String path, @Context SecurityContext sec) {
		
		if (path.contains("/"))
			throw new WebAppExcept("Path invalid", 404);
		
		logger.debug("Trying to load "+RESOURCE_PATH+path);
		
		InputStream stream = StadtRallye.class.getResourceAsStream(RESOURCE_PATH+path);
		
		if (stream==null) {

			throw new WebAppExcept("Not found.", 404);
		}

		MediaType m = DefaultMediaTypePredictor.CommonMediaTypes.getMediaTypeFromFileName(path);
		return Response.ok(stream,m).build();
	
	}
	
	@GET
	public Response index(@Context SecurityContext sec) {
		
		return index("index.html",sec);
		
	}
}
