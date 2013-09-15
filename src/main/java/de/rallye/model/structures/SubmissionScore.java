package de.rallye.model.structures;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class SubmissionScore {
	public final int submissionID;
	public final String score;
	
	@JsonCreator
	public SubmissionScore(@JsonProperty("submissionID")int submissionID, @JsonProperty("score") String score) {
		this.submissionID = submissionID;
		this.score = score;
	}

	@Override
	public String toString() {
		return submissionID + "|"+ score;
	}
}
