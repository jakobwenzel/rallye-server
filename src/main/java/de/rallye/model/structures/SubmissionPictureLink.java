package de.rallye.model.structures;

import java.util.Map;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.push.PushService;

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
		private IDataAdapter data;

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
