package de.rallye.model.structures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PictureLink<T> {
	
	protected static final Logger logger = LogManager.getLogger(PictureLink.class);
	
	public enum Mode {EditObjectWhenUploaded, PictureUploaded }
	
	private Integer pictureID;
	private T linkableObject;

	
	private Mode mode;
	
	public interface ILinkCallback<T> {
		void propagateLink(PictureLink<T> link, Mode mode);
	}
	
	private final ILinkCallback<T> callback;
	
	public PictureLink(ILinkCallback<T> callback) {
		this.callback = callback;
	}
	
	
	public void setPicture(int pictureID) {
		this.pictureID = pictureID;
		
		logger.info("{}: attached picture {}", this, pictureID);
		
		checkLinkComplete();
	}
	
	public void setObject(T obj) {
		this.linkableObject = obj;
		
		logger.info("{}: attached object {}", this, obj.toString());
		
		checkLinkComplete();
	}
	
	public Integer getPictureID() {
		return pictureID;
	}
	
	public T getObj() {
		return linkableObject;
	}
	
	private void checkLinkComplete() {
		if (pictureID != null && pictureID > 0 && linkableObject != null && mode == Mode.EditObjectWhenUploaded) {
			callback.propagateLink(this, mode);
			logger.info("{}: complete -> edit obj", this);
		} else if (pictureID != null && pictureID > 0) {
			mode = Mode.PictureUploaded;
			logger.info("{}: set mode {}", this, mode);
		} else if (linkableObject!=null) {
			mode = Mode.EditObjectWhenUploaded;
			logger.info("{}: set mode {}", this, mode);
		}
	}

}
