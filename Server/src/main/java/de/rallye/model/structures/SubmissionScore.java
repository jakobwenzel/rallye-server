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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

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
