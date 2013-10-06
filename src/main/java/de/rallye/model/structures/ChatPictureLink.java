package de.rallye.model.structures;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.push.PushService;

public class ChatPictureLink {
	
	private static Logger logger = LogManager.getLogger(ChatPictureLink.class);
	
	public enum Mode { EditChatWhenUploaded, PictureUploaded };
	
	private Integer pictureID;
	private Integer roomID;
	private ChatEntry chatEntry;
	
	private Mode mode;
	
	public interface ILinkCallback {
		void propagateLink(ChatPictureLink link, Mode mode, PushService push);
	}
	
	private ILinkCallback callback;
	
	public ChatPictureLink(ILinkCallback callback) {
		this.callback = callback;
	}
	
	
	public void setPicture(int pictureID, PushService push) {
		this.pictureID = pictureID;
		
		logger.info("{}: attached picture {}", this, pictureID);
		
		checkLinkComplete(push);
	}
	
	public void setChat(ChatEntry chatEntry, int roomID, PushService push) {
		this.chatEntry = chatEntry;
		this.roomID = roomID;
		
		logger.info("{}: attached chat {}", this, chatEntry.chatID);
		
		checkLinkComplete(push);
	}
	
	public Integer getPictureID() {
		return pictureID;
	}
	
	public ChatEntry getChat() {
		return chatEntry;
	}
	
	private void checkLinkComplete(PushService push) {
		if (pictureID != null && pictureID > 0 && chatEntry != null && chatEntry.chatID > 0 && mode == Mode.EditChatWhenUploaded) {
			callback.propagateLink(this, mode, push);
			logger.info("{}: complete -> edit chat", this);
		} else if (pictureID != null && pictureID > 0) {
			mode = Mode.PictureUploaded;
			logger.info("{}: set mode {}", this, mode);
		} else if (chatEntry != null && chatEntry.chatID > 0) {
			mode = Mode.EditChatWhenUploaded;
			logger.info("{}: set mode {}", this, mode);
		}
	}
	
	public static ChatPictureLink getLink(Map<String, ChatPictureLink> hashMap, String hash, IDataAdapter data) {
		ChatPictureLink link = hashMap.get(hash);
		if (link == null) {
			link = new ChatPictureLink(new LinkCallback(data));
			hashMap.put(hash, link);
		}
		
		return link;
	}
	
	private static class LinkCallback implements ILinkCallback {
		private IDataAdapter data;
		
		public LinkCallback(IDataAdapter data) {
			this.data = data;
		}

		@Override
		public void propagateLink(ChatPictureLink link, Mode mode, PushService push) {
			try {
				data.editChatAddPicture(link.chatEntry.chatID, link.pictureID);

				push.chatChanged(link.chatEntry, link.roomID);
			} catch (DataException e) {
				logger.error("{}: Linking failed", this, e);
			}
		}
	}
}
