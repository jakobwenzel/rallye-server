package de.rallye.model.structures;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;

public class ChatPictureLink {
	
	private static Logger logger = LogManager.getLogger(ChatPictureLink.class);
	
	public enum Mode { EditChatWhenUploaded, PictureUploaded };
	
	private Integer pictureID;
	private Integer chatID;
	
	private Mode mode;
	
	public interface ILinkCallback {
		void propagateLink(ChatPictureLink link, Mode mode);
	}
	
	private ILinkCallback callback;
	
	public ChatPictureLink(ILinkCallback callback) {
		this.callback = callback;
	}
	
	
	public void setPicture(int pictureID) {
		this.pictureID = pictureID;
		
		logger.info("{}: attached picture {}", this, pictureID);
		
		checkLinkComplete();
	}
	
	public void setChat(int chatID) {
		this.chatID = chatID;
		
		logger.info("{}: attached chat {}", this, chatID);
		
		checkLinkComplete();
	}
	
	public Integer getPictureID() {
		return pictureID;
	}
	
	public Integer getChatID() {
		return chatID;
	}
	
	private void checkLinkComplete() {
		if (pictureID != null && pictureID > 0 && chatID != null && chatID > 0 && mode == Mode.EditChatWhenUploaded) {
			callback.propagateLink(this, mode);
			logger.info("{}: complete -> edit chat", this);
		} else if (pictureID != null && pictureID > 0) {
			mode = Mode.PictureUploaded;
			logger.info("{}: set mode {}", this, mode);
		} else if (chatID != null && chatID > 0) {
			mode = Mode.EditChatWhenUploaded;
			logger.info("{}: set mode {}", this, mode);
		}
	}
	
	public static ChatPictureLink getLink(Map<String, ChatPictureLink> hashMap, String hash, DataAdapter data) {
		ChatPictureLink link = hashMap.get(hash);
		if (link == null) {
			link = new ChatPictureLink(new LinkCallback(data));
			hashMap.put(hash, link);
		}
		
		return link;
	}
	
	private static class LinkCallback implements ILinkCallback {
		private DataAdapter data;
		
		public LinkCallback(DataAdapter data) {
			this.data = data;
		}

		@Override
		public void propagateLink(ChatPictureLink link, Mode mode) {
			try {
				data.editChatAddPicture(link.chatID, link.pictureID);
			} catch (DataException e) {
				logger.error("{}: Linking failed", this, e);
			}
		}
	}
}
