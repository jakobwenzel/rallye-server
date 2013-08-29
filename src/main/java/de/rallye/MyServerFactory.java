package de.rallye;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;


/**
 * This is a copy of the needed methods of GrizzlyServerFactory, only without server.start().
 * @author wilson
 *
 */
public class MyServerFactory {
	public static HttpServer createHttpServer(final URI u,
			final ResourceConfig rc) throws IOException,
			IllegalArgumentException, NullPointerException {
		final HttpHandler handler = ContainerFactory.createContainer(
				HttpHandler.class, rc);
		return createHttpServer(u, handler);
	}
	

    public static HttpServer createHttpServer(
            final URI u,
            final HttpHandler handler)
            throws IOException, IllegalArgumentException, NullPointerException {
        return createHttpServer(u, handler, false, null);
    }
    
    public static HttpServer createHttpServer(
            final URI u,
            final HttpHandler handler,
            final boolean secure,
            final SSLEngineConfigurator sslEngineConfigurator
    ) throws IOException,
            IllegalArgumentException, NullPointerException {

        if (u == null) {
            throw new NullPointerException("The URI must not be null");
        }

        final String scheme = u.getScheme();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("The URI scheme, of the URI " + u
                    + ", must be equal (ignoring case) to 'http' or 'https'");
        }

        final String host = (u.getHost() == null) ? NetworkListener.DEFAULT_NETWORK_HOST
                : u.getHost();
        final int port = (u.getPort() == -1) ? 80 : u.getPort();

        // Create the server.
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly", host, port);
        listener.setSecure(secure);
        if(sslEngineConfigurator != null) {
            listener.setSSLEngineConfig(sslEngineConfigurator);
        }

        server.addListener(listener);

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();
        if (handler != null) {
            config.addHttpHandler(handler, u.getPath());
        }

        return server;
    }
}
