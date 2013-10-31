package de.rallye.model.structures;

import java.util.Map;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.push.PushService;

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
				data.editChatAddPicture(link.getObj().entry.chatID, link.getPictureID());

				link.getObj().push.chatChanged(link.getObj().entry, link.getObj().roomID);
			} catch (DataException e) {
				logger.error("{}: Linking failed", this, e);
			}
		}
	}

}
