package de.rallye.model.structures;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubmissionScore {
	public final int taskID;
	public final int groupID;
	public final int score;
	public final int bonus;
	public final boolean remove;
	
	@JsonCreator
	public SubmissionScore(@JsonProperty("taskID")int taskID, @JsonProperty("groupID")int groupID, @JsonProperty("score") int score, @JsonProperty("bonus") int bonus, @JsonProperty("remove") boolean remove) {
		this.taskID = taskID;
		this.groupID = groupID;
		this.score = score;
		this.bonus = bonus;
		this.remove = remove;
	}

	@Override
	public String toString() {
		return taskID + "|" + groupID + "|" + score+"+"+bonus+"|"+remove;
	}
}
