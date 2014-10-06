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
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.model.structures;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.push.PushService;

import java.util.Map;

class RoomChat {
	final public ChatEntry entry;
	final public int roomID;
	final public PushService push;
	public RoomChat(ChatEntry entry, int roomID, PushService push) {
		this.entry = entry;
		this.roomID = roomID;
		this.push = push;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RoomChat roomChat = (RoomChat) o;

		if (roomID != roomChat.roomID) return false;
		return entry.equals(roomChat.entry);

	}

	@Override
	public int hashCode() {
		int result = entry.hashCode();
		result = 31 * result + roomID;
		return result;
	}
}

/**
 * Created by Jakob Wenzel on 09.10.13.
 */
public class ChatPictureLink extends PictureLink<RoomChat> {


	public ChatPictureLink(ILinkCallback<RoomChat> callback) {
		super(callback);
	}


	public static ChatPictureLink getLink(Map<String, ChatPictureLink> hashMap, String hash, IDataAdapter data) {
		ChatPictureLink link = hashMap.get(hash);
		if (link == null) {
			link = new ChatPictureLink(new LinkCallback(data));
			hashMap.put(hash, link);
		}

		return link;
	}

	public void setChat(ChatEntry entry, int roomID, PushService push) {
		setObject(new RoomChat(entry,roomID,push));
	}

	private static class LinkCallback implements ILinkCallback<RoomChat> {
		private final IDataAdapter data;

		public LinkCallback(IDataAdapter data) {
			this.data = data;
		}

		@Override
		public void propagateLink(PictureLink<RoomChat> link, Mode mode) {
			try {
				data.editChatAddPicture(link.getObj().entry.chatID, link.getPicture().getPictureID());// associate the chatENtry directly with a picId, for increased efficiency

//				link.getObj().push.chatChanged(link.getObj().entry, link.getObj().roomID);//To clients to chat did not change
			} catch (DataException e) {
				logger.error("{}: Linking failed", this, e);
			}
		}
	}

}
