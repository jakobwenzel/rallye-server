package de.rallye.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import de.rallye.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;
import de.rallye.model.structures.PushEntity.Type;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.UserInternal;
import de.rallye.push.IPushAdapter;

/**
 * Pushmodes must be continuous (no unused pushModes) matching a List
 * @author Ramon
 *
 */
public class PushService {
	
	private static Logger logger = LogManager.getLogger(PushService.class);
	
	private Map<Integer, IPushAdapter> pushModes = Collections.synchronizedMap(new HashMap<Integer, IPushAdapter>());

	private DataAdapter data;
//	private ObjectMapper mapper;

	public PushService(DataAdapter data) {
		this.data = data;
//		this.mapper = new ObjectMapper();
		
		try {
			for (PushMode p: data.getPushModes()) {
				pushModes.put(p.pushID, PushService.getPushAdapter(p.name, data));
			}
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	public static IPushAdapter getPushAdapter(String name, DataAdapter data) {
		if (name.equalsIgnoreCase("gcm"))
			return new GCMPushAdapter(RallyeConfig.GCM_API_KEY, data);
		else
			return null;
	}
	
	public void configurationChange() {
		
	}
	
	public void chatChanged(ChatEntry chat, int roomID) {
		try {
			List<UserInternal> users = data.getChatroomMembers(roomID);
			
			push(users, toJSON(chat, roomID), Type.messageChanged);
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	public void chatAdded(ChatEntry chat, int roomID) {
		try {
			List<UserInternal> users = data.getChatroomMembers(roomID);
			
			
			
			push(users, toJSON(chat, roomID), Type.newMessage);
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	private String toJSON(ChatEntry chat, int roomID) {
		JSONObject o = new JSONObject();
		
		try {
			o.put(ChatEntry.CHAT_ID, chat.chatID)
				.put(ChatEntry.GROUP_ID, chat.groupID)
				.put(Chatroom.CHATROOM_ID, roomID)
				.put(ChatEntry.USER_ID, chat.userID)
				.put(ChatEntry.MESSAGE, chat.message)
				.put(ChatEntry.PICTURE_ID, chat.pictureID)
				.put(ChatEntry.TIMESTAMP, chat.timestamp);
		} catch (JSONException e) {
			logger.error(e);
		}
		
		return o.toString();
	}
	
	private void push(List<UserInternal> users, String payload, Type type) {
		HashMap<Integer, List<UserInternal>> ids = new HashMap<Integer, List<UserInternal>>();
		
		Set<Integer> modes = pushModes.keySet();
		for (int m: modes) {
			ids.put(m, new ArrayList<UserInternal>());
		}
		
		for (UserInternal u: users) {
			int mode = u.pushMode;
			
			if (mode > 0) {
				List<UserInternal> list = ids.get(mode);
				
				if (list != null) {
					list.add(u);
				} else {
					logger.error("PushMode {} not supported for User {}", mode, u);
				}
			} else {
				//[Client with no push support] TODO: save changes until client requests a list of changes
			}
		}
		
		for (Entry<Integer, IPushAdapter> m: pushModes.entrySet()) {
			m.getValue().push(ids.get(m.getKey()), payload, type);
		}
	}
}
