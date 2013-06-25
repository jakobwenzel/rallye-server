package de.rallye;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import de.rallye.push.PushWebsocketApp;

public class RallyeServer {
	
	// this is needed to minimize the logging from Jersey in console
	private final static java.util.logging.Logger COM_LOGGER = java.util.logging.Logger.getLogger("com");
	private final static java.util.logging.Logger ORG_LOGGER = java.util.logging.Logger.getLogger("org");
	static {
		//ORG_LOGGER.setLevel(Level.SEVERE);
		//COM_LOGGER.setLevel(Level.SEVERE);
	}
	
	private static final Logger logger = LogManager.getLogger(RallyeServer.class);

	private HttpServer httpServer = null;
	
	private static RallyeResources resources;

	public RallyeServer(String host, int port, RallyeResources resources) {
		logger.entry();

		// create URI
		URI uri = UriBuilder.fromUri("http://" + host + "/").port(10101).build();
		
		
		this.resources = resources;

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
		
		//
		HttpServer serv = MyServerFactory.createHttpServer(uri, rc);
        //final HttpServer serv = HttpServer.createSimpleServer("localhost",10101);
		logger.info("Starting Grizzly server at " + uri);

		//Register Websocket Stuff
		WebSocketAddOn addon = new WebSocketAddOn();
		for(org.glassfish.grizzly.http.server.NetworkListener listener : serv.getListeners()) {
			System.out.println("registering on "+listener);
			listener.registerAddOn(addon);
		}
		
		WebSocketApplication pushApp = new PushWebsocketApp();
		WebSocketEngine.getEngine().register("/grizzly-websockets-chat", "/chat", pushApp);

		serv.start();  
		return serv;
	}
	
    public static final int PORT = 8080;
    
    public static void mainWorking(String[] args) throws Exception {
        // Server expects to get the path to webapp as command line parameter
        if (args.length < 1) {
            System.out.println("Please provide a path to webapp in the command line");
            System.exit(0);
        }
        // create a Grizzly HttpServer to server static resources from 'webapp', on PORT.
        final HttpServer server = HttpServer.createSimpleServer(args[0], PORT);

        // Register the WebSockets add on with the HttpServer
        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());

        // initialize websocket chat application
        final WebSocketApplication chatApplication = new PushWebsocketApp();

        // register the application
        WebSocketEngine.getEngine().register("/grizzly-websockets-chat", "/chat", chatApplication);

        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } finally {
            // stop the server
            server.stop();
        }
    }
	
	public static void mainBroken(String[] args) throws Exception {
        final HttpServer server = HttpServer.createSimpleServer("localhost", 8080);

        // Register the WebSockets add on with the HttpServer
        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());

        // initialize websocket chat application
        final WebSocketApplication chatApplication = new PushWebsocketApp();

        // register the application
        WebSocketEngine.getEngine().register("/grizzly-websockets-chat", "/chat", chatApplication);
        
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } finally {
            // stop the server
            server.stop();
        }

	}
	
	public static void main(String[] args) throws Exception{
		mainBroken(args);
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
