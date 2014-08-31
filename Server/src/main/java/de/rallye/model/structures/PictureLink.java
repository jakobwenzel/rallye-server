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
