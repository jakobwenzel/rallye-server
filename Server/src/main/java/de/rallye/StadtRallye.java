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

package de.rallye;

import de.rallye.admin.ServerConsole;
import de.rallye.config.ConfigTools;
import de.rallye.config.GitRepositoryState;
import de.rallye.config.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.injection.RallyeBinder;
import de.rallye.model.structures.RallyeGameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.logging.Level;

public class StadtRallye {

	// this is needed to minimize the logging from Jersey in console
	private final static java.util.logging.Logger COM_LOGGER = java.util.logging.Logger.getLogger("com");
	private final static java.util.logging.Logger ORG_LOGGER = java.util.logging.Logger.getLogger("org");
	static {
		ORG_LOGGER.setLevel(Level.SEVERE);
		COM_LOGGER.setLevel(Level.SEVERE);
	}

	private static final Logger logger = LogManager.getLogger(StadtRallye.class);

	public static void main(String[] args) {

		logger.info("Starting RallyeServer");

		//Init resources
		RallyeConfig config = RallyeConfig.fromFile(ConfigTools.findConfigFile(), GitRepositoryState.getState());
		//If there are config errors, we exit.
		if (config!=null) {
			RallyeBinder.config = config;
			RallyeBinder.data = DataAdapter.getInstance(config);
			RallyeBinder.gameState = RallyeGameState.getInstance(RallyeBinder.data);
			
			//start server
			String host = (args.length > 0 ? args[0] : config.getHostName());
			RallyeServer server = new RallyeServer(host, config.getRestPort());
	
			ServerConsole console = new ServerConsole(config.getConsolePort(), server);
			console.start();
		} else 
			logger.error("Please provide a valid config file. Exiting.");
	}
}
