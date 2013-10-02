package de.rallye.push;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;


/**
 * Custom WebSocket that saves a reference to the user
 */
public class PushWebSocket extends DefaultWebSocket {
    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PushWebSocket.class);
    
    // chat user name
    private volatile int user;

    public PushWebSocket(ProtocolHandler protocolHandler,
                         HttpRequestPacket request,
                         WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    /**
     * Get the user id
     * @return the user id
     */
    public int getUser() {
        return user;
    }

    /**
     * Set the user id
     * @param user the user id
     */
    public void setUser(int user) {
        this.user = user;
    }
}