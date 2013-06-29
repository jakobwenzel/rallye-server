package de.rallye.push;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

import de.rallye.model.structures.User;


/**
 * Custom WebSocket that saves a reference to the user
 */
public class PushWebSocket extends DefaultWebSocket {
    private static final Logger logger = LogManager.getLogger(PushWebSocket.class);
    
    // chat user name
    private volatile User user;

    public PushWebSocket(ProtocolHandler protocolHandler,
                         HttpRequestPacket request,
                         WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    /**
     * Get the user name
     * @return the user name
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the user name
     * @param user the user name
     */
    public void setUser(User user) {
        this.user = user;
    }
}