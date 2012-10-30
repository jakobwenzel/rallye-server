package de.stadtrallye.control;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import de.stadtrallye.model.DataHandler;
import com.google.android.gcm.server.*;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

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

	public GameHandler() {
		// create and init new DataHander
		data = new DataHandler();

		// create URI
		BASE_URI = UriBuilder.fromUri("http://localhost/").port(10101).build();

		// start server
		try {
			httpServer = startServer();
			System.out.println(String.format("Jersey app started with WADL available at "
				+ "%sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...", BASE_URI, BASE_URI));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Google GCM, snippet
	 */
	public void push() {
		Sender sender = new Sender("AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0");
		Message msg = new Message.Builder().build();
		try {
			Result res = sender.send(msg, "", 3);// RegIds

			if (res.getMessageId() != null) {
				String canonicalRegId = res.getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					// same device has more than on registration ID: update
					// database
				}
			} else {
				String error = res.getErrorCodeName();
				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
					// application has been removed from device - unregister
					// database
				}
			}
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
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	/**
	 * 
	 * 
	 * @author Felix HŸbner
	 */
	public void stopServer() {
		if (this.httpServer != null) {
			this.httpServer.stop();
			System.out.println("Server on url:" + BASE_URI + " Stopped!");
		}
	}
}
