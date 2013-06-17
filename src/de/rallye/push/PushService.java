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
import org.codehaus.jackson.node.ObjectNode;

import de.rallye.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;
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
	private ObjectMapper mapper;

	public PushService(DataAdapter data) {
		this.data = data;
		this.mapper = new ObjectMapper();
		
		try {
			for (PushMode p: data.getPushModes()) {
				pushModes.put(p.pushID, PushService.getPushAdapter(p.name, data));
			}
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	public static IPushAdapter getPushAdapter(String name, DataAdapter data) {
		if (name.equals("gcm"))
			return new GCMPushAdapter(RallyeConfig.GCM_API_KEY, data);
		else
			return null;
	}
	
	public void configurationChange() {
		
	}
	
	public void chatChanged(ChatEntry chat) {
		
	}
	
	public String test() {
		String res = toJSON(new ChatEntry(1, "message", 0l, 3, 282, null), 4);
		logger.info("test", res);
		return res;
	}
	
	public void chatAdded(ChatEntry chat, int roomID) {
		try {
			List<UserInternal> usrs = data.getChatroomMembers(roomID);
			
			
			
			push(usrs, toJSON(chat, roomID));
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	private String toJSON(ChatEntry chat, int roomID) {
		ObjectNode o = mapper.createObjectNode();
		o.put(Chatroom.CHATROOM_ID, roomID);
		o.putPOJO("chat", chat);
		
		return o.toString();
	}
	
	private void push(List<UserInternal> users, String payload) {
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
			m.getValue().push(ids.get(m.getKey()), payload);
		}
	}
}
