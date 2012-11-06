package de.stadtrallye.control;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import de.stadtrallye.model.DataHandler;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class GameHandler {
	DataHandler data;
	ClientListener listener;
	private static URI BASE_URI = null;
	private HttpServer httpServer = null;
	
	
	//this is needed to minimise the logging from Jersey in console
	private final static Logger COM_SUN_JERSEY_LOGGER = Logger.getLogger( "com.sun.jersey" ); 
	static { COM_SUN_JERSEY_LOGGER.setLevel( Level.SEVERE ); }

	public GameHandler() {
		// create and init new DataHander
		data = new DataHandler();
		
		// create URI 
		//TODO get uri and port from DataHandler
		BASE_URI = UriBuilder.fromUri("http://"+this.data.getUri()+"/").port(this.data.getPort()).build();

		// start server
		try {
			httpServer = startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 * @author Felix HŸbner
	 */
	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		
		ResourceConfig rc = new PackagesResourceConfig("de.stadtrallye.control");
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		rc.getFeatures().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, true);
		rc.getFeatures().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, true);
		rc.getContainerRequestFilters().add(new GZIPContentEncodingFilter());
		rc.getContainerResponseFilters().add(new GZIPContentEncodingFilter());
		HttpServer serv = GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
		return serv;
	}
	

	/**
	 * 
	 * 
	 * @author Felix HŸbner
	 */
	public void stopServer() {
		if (this.httpServer != null) {
			this.httpServer.stop();
		}
	}

	
}
