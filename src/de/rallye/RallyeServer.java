package de.rallye;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.push.PushService;

public class RallyeServer {
	
	// this is needed to minimize the logging from Jersey in console
	private final static java.util.logging.Logger COM_LOGGER = java.util.logging.Logger.getLogger("com");
	private final static java.util.logging.Logger ORG_LOGGER = java.util.logging.Logger.getLogger("org");
	static {
		ORG_LOGGER.setLevel(Level.SEVERE);
		COM_LOGGER.setLevel(Level.SEVERE);
	}
	
	private static final Logger logger = LogManager.getLogger(RallyeServer.class);

	private HttpServer httpServer = null;
	
	private static RallyeResources resources;

	public RallyeServer(String host, int port, DataAdapter data, ImageRepository imgRepo, PushService push) {
		logger.entry();

		// create URI
		URI uri = UriBuilder.fromUri("http://" + host + "/").port(10101).build();
		
		Map<String, ChatPictureLink> hashmap = Collections.synchronizedMap(new HashMap<String, ChatPictureLink>());
		
		resources = new RallyeResources(data, imgRepo, hashmap, push);

		// start http server
		try {
			httpServer = startServer(uri);
		} catch (IOException e) {
			logger.catching(e);
		}
		
		logger.exit();
	}
	
	public static RallyeResources getResources() {
		return resources;
	}

	@SuppressWarnings("unchecked")
	private HttpServer startServer(URI uri) throws IOException {
		ResourceConfig rc = new PackagesResourceConfig("de.rallye.api");
		rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
		rc.getFeatures().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, true);
		rc.getFeatures().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, true);
		rc.getContainerRequestFilters().add(new GZIPContentEncodingFilter());
		rc.getContainerResponseFilters().add(new GZIPContentEncodingFilter());
		HttpServer serv = GrizzlyServerFactory.createHttpServer(uri, rc);
		logger.info("Starting Grizzly server at " + uri);
		return serv;
	}

	/**
	 * 
	 * 
	 * @author Felix H�bner
	 */
	public void stopServer() {
		logger.info("Stopping Grizzly server");
		
		resources.data.closeConnection();
		
		this.httpServer.stop();
	}

}
