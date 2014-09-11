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

import java.util.Map;

/**
 * Created by Jakob Wenzel on 09.10.13.
 */
public class SubmissionPictureLink extends PictureLink<Submission>{
	public SubmissionPictureLink(PictureLink.ILinkCallback<Submission> callback) {
		super(callback);
	}


	public static SubmissionPictureLink getLink(Map<String, SubmissionPictureLink> hashMap, String hash, IDataAdapter data) {
		SubmissionPictureLink link = hashMap.get(hash);
		if (link == null) {
			link = new SubmissionPictureLink(new LinkCallback(data));
			hashMap.put(hash, link);
		}

		return link;
	}

	private static class LinkCallback implements PictureLink.ILinkCallback<Submission> {
		private final IDataAdapter data;

		public LinkCallback(IDataAdapter data) {
			this.data = data;
		}

		@Override
		public void propagateLink(PictureLink<Submission> link, PictureLink.Mode mode) {
			try {
				logger.debug("Adding pic {} to submission {}",link.getPictureID(),link.getObj().submissionID);
				data.editSubmissionAddPicture(link.getObj().submissionID, link.getPictureID());
			} catch (DataException e) {
				logger.error("Error adding picture to submission");
				e.printStackTrace();
			}
		}
	}

}
