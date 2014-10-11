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
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

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