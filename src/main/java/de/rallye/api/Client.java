package de.rallye.api;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

import java.net.URL;

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
import de.rallye.StadtRallye;
import de.rallye.exceptions.WebAppExcept;

@Path("client")
public class Client {
	private static final String RESOURCE_PATH = "webclient/";
	private RallyeResources R = RallyeResources.getResources();

	private Logger logger =  LogManager.getLogger(Client.class);
	
	boolean projectChecked = false;
	String projectDir;

//	private RallyeResources R = RallyeServer.getResources();

	@GET
	@Path("{path}")
	public Response index(@PathParam("path") String path, @Context SecurityContext sec) {
		
		if (path.contains("/"))
			throw new WebAppExcept("Path invalid", 404);
		
		logger.debug("Trying to load "+RESOURCE_PATH+path);
		
		//If we are running from project dir, always load files from src directory to avoid recompiles during development
		if (!projectChecked) {
			projectDir = R.getProjectDir();
			projectChecked = true;
		}
		
		InputStream stream;
		if (projectDir!=null) {
			logger.debug("serving from project dir");
			try {
				stream = new FileInputStream(new File(new URL(projectDir+"src/main/resources/de/rallye/"+RESOURCE_PATH+path).toURI()));
			} catch(Exception e) {
				throw new WebAppExcept(e);
			}
		} else {
		
			logger.debug("serving from jar");
			stream = StadtRallye.class.getResourceAsStream(RESOURCE_PATH+path);
		}
		
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
