package de.rallye;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
//import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import de.rallye.admin.AdminWebsocketApp;
import de.rallye.filter.auth.EnsureMimeType;
import de.rallye.injection.RallyeBinder;
import de.rallye.push.PushWebsocketApp;

public class RallyeServer {
	
	private static final Logger logger = LogManager.getLogger(RallyeServer.class);

	public RallyeServer(String host, int port) {
		logger.entry();

		// create URI
		URI uri = UriBuilder.fromUri("http://" + host + "/").port(port).build();
		
		

		// start http server
		try {
			startServer(uri);
		} catch (IOException e) {
			logger.catching(e);
		}
		
		logger.exit();
	}


	private HttpServer startServer(URI uri) throws IOException {
		ResourceConfig rc = new ResourceConfig();
		rc.packages("de.rallye.api", "de.rallye.filter", "de.rallye.filter.auth","de.rallye.exceptions.mappers");
		rc.register(JacksonFeature.class);
		rc.register(EnsureMimeType.class);
		rc.register(new RallyeBinder());     
	    rc.register(MultiPartFeature.class);

		HttpServer serv = createServer(uri, rc); // Do NOT start the server just yet
		logger.info("Starting Grizzly server at " + uri);

		//Register Websocket Stuff
		WebSocketAddOn addon = new WebSocketAddOn();
		for(org.glassfish.grizzly.http.server.NetworkListener listener : serv.getListeners()) {
			logger.info("registering websocket on "+listener);
			listener.registerAddOn(addon);
		}
		PushWebsocketApp.setData(RallyeBinder.data); //TODO: Remove this ugliness.
		AdminWebsocketApp.setData(RallyeBinder.data); //TODO: Remove this ugliness.
		WebSocketEngine.getEngine().register("/rallye", "/push", PushWebsocketApp.getInstance());
		WebSocketEngine.getEngine().register("/rallye","/admin", AdminWebsocketApp.getInstance());

		serv.start();  
		return serv;
	}

	@SuppressWarnings("unused")
	private HttpServer createServer(URI uri, ResourceConfig configuration) {
		GrizzlyHttpContainer handler = ContainerFactory.createContainer(GrizzlyHttpContainer.class, configuration);
		boolean secure = false;
		SSLEngineConfigurator sslEngineConfigurator = null;
		boolean start = false;

		final String host = (uri.getHost() == null) ? NetworkListener.DEFAULT_NETWORK_HOST
				: uri.getHost();
		final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
		final HttpServer server = new HttpServer();
		final NetworkListener listener = new NetworkListener("grizzly", host, port);
		listener.setSecure(secure);
		if (sslEngineConfigurator != null) {
			listener.setSSLEngineConfig(sslEngineConfigurator);
		}

		// insert
//		listener.

		CompressionConfig cg = listener.getCompressionConfig();
		cg.setCompressionMode(CompressionConfig.CompressionMode.ON);
//		cg.setCompressableMimeTypes("text","application/json");
		// end insert

		server.addListener(listener);

		// Map the path to the processor.
		final ServerConfiguration config = server.getServerConfiguration();
		if (handler != null) {
			config.addHttpHandler(handler, uri.getPath());
		}

		config.setPassTraceRequest(true);

		if (start) {
			try {
				// Start the server.
				server.start();
			} catch (IOException ex) {
				throw new ProcessingException("IOException thrown when trying to start grizzly server", ex);
			}
		}

		return server;
	}

	public void stopServer() {
		logger.info("Stopping Grizzly server");
		
		//this.httpServer.shutdown();
	}

}
