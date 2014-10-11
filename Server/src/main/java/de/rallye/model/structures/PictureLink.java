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
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.model.structures;

import de.rallye.images.ImageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PictureLink<T> {
	
	protected static final Logger logger = LogManager.getLogger(PictureLink.class);
	
	public enum Mode {EditObjectWhenUploaded, PictureUploaded }
	
	private ImageRepository.Picture picture;
	private T linkableObject;

	
	private Mode mode;
	
	public interface ILinkCallback<T> {
		void propagateLink(PictureLink<T> link, Mode mode);
	}
	
	private final ILinkCallback<T> callback;
	
	public PictureLink(ILinkCallback<T> callback) {
		this.callback = callback;
	}
	
	
	public synchronized void setPicture(ImageRepository.Picture picture) {
		this.picture = picture;
		
		logger.info("{}: attached picture {}", this, picture);
		
		checkLinkComplete();
	}
	
	public synchronized void setObject(T obj) {
		this.linkableObject = obj;
		
		logger.info("{}: attached object {}", this, obj.toString());
		
		checkLinkComplete();
	}
	
	public synchronized ImageRepository.Picture getPicture() {
		return picture;
	}
	
	public synchronized T getObj() {
		return linkableObject;
	}
	
	private synchronized void checkLinkComplete() {
		if (picture != null && linkableObject != null && mode == Mode.EditObjectWhenUploaded) {
			callback.propagateLink(this, mode);
			logger.info("{}: complete -> edit obj", this);
		} else if (picture != null) {
			mode = Mode.PictureUploaded;
			logger.info("{}: set mode {}", this, mode);
		} else if (linkableObject!=null) {
			mode = Mode.EditObjectWhenUploaded;
			logger.info("{}: set mode {}", this, mode);
		}
	}

}
