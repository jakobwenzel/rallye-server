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

package de.rallye.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.PushChatEntry;
import de.rallye.model.structures.PushEntity.Type;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.UserInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;

/**
 * Pushmodes must be continuous (no unused pushModes) matching a List
 * @author Ramon
 *
 */
public class PushService {
	
	private final Logger logger = LogManager.getLogger(PushService.class);
	
	private final Map<Integer, IPushAdapter> pushModes = Collections.synchronizedMap(new HashMap<Integer, IPushAdapter>());

	private final IDataAdapter data;
	private final ObjectMapper mapper;

	@Inject
	public PushService(IDataAdapter data, RallyeConfig config) {
		this.data = data;
		this.mapper = new ObjectMapper();
		
		try {
			for (PushMode p: data.getPushModes()) {
				pushModes.put(p.pushID, PushService.getPushAdapter(p.name, data, config));
			}
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	public static IPushAdapter getPushAdapter(String name, IDataAdapter data, RallyeConfig config) {
		if (name.equalsIgnoreCase("gcm"))
			return new GCMPushAdapter(config.getGcmApiKey(), data);
		if (name.equalsIgnoreCase("websocket"))
			return PushWebsocketApp.getInstance();
		else
			return null;
	}
	
	public void configurationChange() {
		
	}
	
	public void chatChanged(ChatEntry chat, int roomID) {
		try {
			List<UserInternal> users = data.getChatroomMembers(roomID);
			
			push(users, toJSON(chat, roomID), Type.messageChanged);
		} catch (DataException | JsonProcessingException e) {
			logger.error(e);
		}
	}
	
	public void chatAdded(ChatEntry chat, int roomID) {
		try {
			List<UserInternal> users = data.getChatroomMembers(roomID);
			
			push(users, toJSON(chat, roomID), Type.newMessage);
		} catch (DataException | JsonProcessingException e) {
			logger.error(e);
		}
	}

	public void pingLocation(List<Integer> clientIDs) {
		List<UserInternal> users = new ArrayList<>();
		for (int clientID : clientIDs) {
			UserInternal user = null;
			try {
				user = data.resolveClient(clientID);
			} catch (DataException e) {
				logger.catching(e);
			}
			if (user != null)
				users.add(user);
		}
		push(users, null, Type.pingLocation);
	}

//	public void pingLocation(int groupID) {
//
//	}
	
	private String toJSON(ChatEntry chat, int roomID) throws JsonProcessingException {

		PushChatEntry push = new PushChatEntry(chat, roomID);
		return mapper.writeValueAsString(push);

//		JSONObject o = new JSONObject();
		
//		try {
//			o.put(ChatEntry.CHAT_ID, chat.chatID)
//				.put(ChatEntry.GROUP_ID, chat.groupID)
//				.put(Chatroom.CHATROOM_ID, roomID)
//				.put(ChatEntry.USER_ID, chat.userID)
//				.put(ChatEntry.MESSAGE, chat.message)
//				.put(ChatEntry.PICTURE_ID, chat.pictureHash)
//				.put(ChatEntry.TIMESTAMP, chat.timestamp);
//		} catch (JSONException e) {
//			logger.error(e);
//		}
	}
	
	private void push(List<UserInternal> users, String payload, Type type) {
		logger.info("Pushing {}:{}", type, payload);
		
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
			IPushAdapter adapter = m.getValue();
			if (adapter == null) {
				logger.error("PushAdapter for mode:{} is null", m.getKey());
				continue;
			}
			logger.info("PushMode:{} to {}", m.getKey(), ids.get(m.getKey()));
			adapter.push(ids.get(m.getKey()), payload, type);
		}
	}
}
