/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
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
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.model.structures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by wilson on 05.10.13.
 */
public class RallyeGameState {

	private static final Logger logger = LogManager.getLogger(RallyeGameState.class);

	protected RallyeConfig config;
	static private RallyeGameState instance;
	protected boolean showRatingToUsers;
	protected boolean canSubmit;

	public boolean isShowRatingToUsers() {
		return showRatingToUsers;
	}

	public boolean isCanSubmit() {
		return canSubmit;
	}

	/**
	 * Create a new GameState. Only to be called by DataAdapter
	 *
	 * @param showRatingToUsers
	 */
	@JsonCreator
	public RallyeGameState(@JsonProperty("showRatingToUsers") boolean showRatingToUsers, @JsonProperty("canSubmit") boolean canSubmit) {

		this.showRatingToUsers = showRatingToUsers;
		this.canSubmit = canSubmit;

	}

	public static RallyeGameState getInstance(IDataAdapter data) {
		if (instance == null) {
			try {
				instance = data.loadGameState();
			} catch (DataException e) {
				logger.error(e);
				instance = null;
			}
			if (instance == null)
				instance = new RallyeGameState(false, true);
		}

		return instance;
	}

	public void copy(RallyeGameState state) {
		this.canSubmit = state.canSubmit;
		this.showRatingToUsers = state.showRatingToUsers;
	}
}
