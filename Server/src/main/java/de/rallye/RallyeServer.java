/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye;

import com.fasterxml.jackson.jaxrs.smile.JacksonSmileProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
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
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;

import de.rallye.admin.AdminWebsocketApp;
import de.rallye.injection.RallyeBinder;
import de.rallye.push.PushWebsocketApp;

//import org.glassfish.grizzly.http.CompressionConfig;

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
		ResourceConfig rc = new RallyeApplication();
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
