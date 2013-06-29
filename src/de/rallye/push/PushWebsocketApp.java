package de.rallye.push;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
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

import de.rallye.model.structures.User;
import de.rallye.model.structures.UserInternal;

public class PushWebsocketApp extends WebSocketApplication implements
		IPushAdapter {
	private static final Logger logger = LogManager
			.getLogger(PushWebsocketApp.class);

	Map<User, WebSocket> sockets = new ConcurrentHashMap<User, WebSocket>();

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
		ObjectMapper mapper = (new ObjectMapper());
		try {
			ClientMessage message = mapper.readValue(data, ClientMessage.class);
			message.handleMessage(websocket);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	@Override
	public void push(List<UserInternal> users, String payload,
			de.rallye.model.structures.PushEntity.Type type) {
		// TODO Auto-generated method stub

	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({ @Type(value = LoginMessage.class, name = "login") })
	abstract class ClientMessage {
		public String type;

		abstract void handleMessage(WebSocket socket);
	}

	class LoginMessage extends ClientMessage {
		public String username;
		public String password;

		@Override
		void handleMessage(WebSocket socket) {
			logger.debug("Logging in with " + username + " " + password);
		}
	}
}
