package de.rallye.push;

import java.awt.Frame;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import com.sun.jersey.spi.container.ContainerRequest;

import de.rallye.RallyeServer;
import de.rallye.auth.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.model.structures.User;
import de.rallye.model.structures.UserInternal;

public class PushWebsocketApp extends WebSocketApplication implements
		IPushAdapter {
	private static final Logger logger = LogManager
			.getLogger(PushWebsocketApp.class);

	//Map from UserID to associated Socket
	Map<Integer, WebSocket> sockets = new ConcurrentHashMap<Integer, WebSocket>();

	private PushWebsocketApp() {

	}

	private static PushWebsocketApp instance = new PushWebsocketApp();

	public static PushWebsocketApp getInstance() {
		return instance;
	}

	/**
	 * Creates a customized {@link WebSocket} implementation.
	 * 
	 * @return customized {@link WebSocket} implementation -
	 *         {@link PushWebSocket}
	 */
	@Override
	public WebSocket createSocket(ProtocolHandler handler,
			HttpRequestPacket request, WebSocketListener... listeners) {
		return new PushWebSocket(handler, request, listeners);
	}

	/**
	 * Method is called, when {@link PushWebSocket} receives a {@link Frame}.
	 * 
	 * @param websocket
	 *            {@link PushWebSocket}
	 * @param data
	 *            {@link Frame}
	 * 
	 * @throws IOException
	 */
	@Override
	public void onMessage(WebSocket websocket, String data) {
		try {
			System.out.println(data);
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

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClose(WebSocket websocket, DataFrame frame) {

	}

	private static String escape(String orig) {
		StringBuilder buffer = new StringBuilder(orig.length());

		for (int i = 0; i < orig.length(); i++) {
			char c = orig.charAt(i);
			switch (c) {
			case '\b':
				buffer.append("\\b");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			case '\n':
				buffer.append("<br />");
				break;
			case '\r':
				// ignore
				break;
			case '\t':
				buffer.append("\\t");
				break;
			case '\'':
				buffer.append("\\'");
				break;
			case '\"':
				buffer.append("\\\"");
				break;
			case '\\':
				buffer.append("\\\\");
				break;
			case '<':
				buffer.append("&lt;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '&':
				buffer.append("&amp;");
				break;
			default:
				buffer.append(c);
			}
		}

		return buffer.toString();
	}

	static class PushMessage{
		public String payload;
		public de.rallye.model.structures.PushEntity.Type type;
	}

	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void push(List<UserInternal> users, String payload,
			de.rallye.model.structures.PushEntity.Type type) {
		PushMessage msg = new PushMessage();
		msg.payload = payload;
		msg.type = type;
		
		try {
			String send = mapper.writeValueAsString(msg);
			for (UserInternal user : users) {
				WebSocket socket = sockets.get(user.userID);
				if (socket==null) {
					logger.warn("Push user without socket: "+user.userID);
					continue;
				}
				if (!socket.isConnected()){
					logger.warn("Push user with disconnected socket: "+user.userID);
					continue;
				}
				socket.send(send);
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({ @Type(value = LoginMessage.class, name = "login") })
	static abstract class ClientMessage {
		public String type;
		@JsonIgnore
		PushWebsocketApp app;

		abstract void handleMessage(WebSocket socket);
	}

	static class LoginMessage extends ClientMessage {
		public String username;
		public String password;

		@Override
		void handleMessage(WebSocket socket) {
			try {
				logger.debug("Trying to authenticate user");
				
				KnownUserAuth auth = new KnownUserAuth();
				RallyePrincipal p = auth.checkAuthentication(new String[]{username, password});
			
				int userID = p.getUserID();
				logger.debug("User authenticated as "+userID);
					
				socket.send("{\"type\":\"login\", \"state\": \"ok\"}");
				
				
				app.sockets.put(userID,socket);
				((PushWebSocket)socket).setUser(userID);
						
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
}
