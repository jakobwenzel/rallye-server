package de.rallye.admin;

import java.awt.Frame;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import de.rallye.db.IDataAdapter;
import de.rallye.filter.auth.AdminAuthFilter;
import de.rallye.filter.auth.AdminPrincipal;
import de.rallye.model.structures.Submission;
import de.rallye.push.PushWebSocket;

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
	@JsonSubTypes({ @Type(value = LoginMessage.class, name = "login") })
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
