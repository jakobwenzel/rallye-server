/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
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
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rallye.db.IDataAdapter;
import de.rallye.filter.auth.AdminAuthFilter;
import de.rallye.filter.auth.AdminPrincipal;
import de.rallye.model.structures.Submission;
import de.rallye.push.PushWebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.*;

import javax.ws.rs.WebApplicationException;
import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdminWebsocketApp extends WebSocketApplication {
	Map<Integer, WebSocket> sockets = new ConcurrentHashMap<Integer, WebSocket>();


	private static final Logger logger = LogManager
			.getLogger(AdminWebsocketApp.class);
	
	Set<AdminWebSocket> activeSockets = new HashSet<AdminWebSocket>();
	
	@Override
	public boolean isApplicationRequest(HttpRequestPacket request) {
		logger.info("check for uri "+request.getRequestURI());
	    return "/rallye/push".equals(request.getRequestURI());
	}
	private static AdminWebsocketApp instance = new AdminWebsocketApp();

	public static AdminWebsocketApp getInstance() {
		
		return instance;
	}
	


	/**
	 * Method is called, when {@link PushWebSocket} receives a {@link Frame}.
	 * 
	 * @param websocket
	 *            {@link PushWebSocket}
	 * @param data
	 *            {@link Frame}
	 */
	@Override
	public void onMessage(WebSocket websocket, String data) {
		try {
			logger.info(data);
			ClientMessage message = mapper.readValue(data, ClientMessage.class);
			message.app = this;
			message.handleMessage(websocket);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onConnect(WebSocket socket) {
		activeSockets.add((AdminWebSocket)socket);
		logger.info("new websocket connection");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClose(WebSocket websocket, DataFrame frame) {
		activeSockets.remove(websocket);
		logger.info("websocket closed");
	}
	
	ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unused")
	private static IDataAdapter data;
	

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({ @JsonSubTypes.Type(value = LoginMessage.class, name = "login") })
	static abstract class ClientMessage {
		public String type;
		@JsonIgnore
		AdminWebsocketApp app;

		abstract void handleMessage(WebSocket socket);
	}

	static class LoginMessage extends ClientMessage {
		public String username;
		public String password;

		@Override
		void handleMessage(WebSocket socket) {
			try {
				logger.debug("Trying to authenticate user");
				
				AdminAuthFilter auth = new AdminAuthFilter(data);
				@SuppressWarnings("unused")
				AdminPrincipal p = auth.checkAuthentication(new String[]{username, password});
			
				logger.debug("User authenticated as admin");
				
				((AdminWebSocket)socket).isLoggedIn = true;
					
				socket.send("{\"type\":\"login\", \"state\": \"ok\"}");
				
						
			} catch (WebApplicationException e) {
				socket.send("{\"type\":\"login\", \"state\": \"fail\", \"message\": \"Unauthorized.\"}");
				logger.debug("User unauthorized");
			} catch (Exception e) {
				socket.send("{\"type\":\"login\", \"state\": \"error\", \"message\": \""
						+ e.getMessage() + "\"}");
				e.printStackTrace();
			}
			logger.debug("Logging in with " + username + " " + password);
		}
	}

	public static void setData(IDataAdapter data) {
		AdminWebsocketApp.data = data;		
	}


	public void newSubmission(int groupID, int userID, int taskID, Submission submission) {
		String data;
		try {
			data = "{\"type\":\"submission\",\"data\":"+
				mapper.writeValueAsString(submission)+",\"groupID\":"+groupID+",\"taskID\":"+taskID+",\"userID\":"+userID+"}";
			

			for (AdminWebSocket s: activeSockets) {
				if (s.isLoggedIn) {
					s.send(data);
				}
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket request, WebSocketListener... listeners) {
		return new AdminWebSocket(handler, request, listeners);
	}

	
	class AdminWebSocket extends DefaultWebSocket {
		public boolean isLoggedIn = false;
		public AdminWebSocket(ProtocolHandler arg0, HttpRequestPacket arg1,
				WebSocketListener[] arg2) {
			super(arg0, arg1, arg2);
		}
		
	}
}
