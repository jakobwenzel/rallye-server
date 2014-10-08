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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.Map;

/**
 * Created by Ramon on 08.10.2014.
 */
public class GeoPostSubmission extends PostSubmission {

	public final java.util.Map<String, String> location;

	public GeoPostSubmission(@JsonProperty("submitType") int submitType, @JsonProperty("picSubmission") String picSubmission, @JsonProperty("intSubmission") Integer intSubmission, @JsonProperty("textSubmission") String textSubmission, @JsonProperty("location") Map<String, String> location) {
		super(submitType, picSubmission, intSubmission, textSubmission);
		this.location = location;
	}
}
