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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rallye.test.helper;

import de.rallye.RallyeServer;
import de.rallye.config.RallyeConfig;
import de.rallye.injection.RallyeBinder;
import de.rallye.model.structures.RallyeGameState;
import de.rallye.test.db.MockDataAdapter;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
/**
 *
 * @author Jakob Wenzel
 */
public class StartTestServer {
	static RallyeServer server = null;
	public static RallyeServer getServer() {
		if (server==null) 
			startServer();
		
		return server;
	}
	
	private static void startServer() {
		server = getServerWithConfig("config.json");
	}
	
	public static RallyeServer getServerWithConfig(String configName) {
		//Init resources
		InputStream stream = StartTestServer.class.getResourceAsStream(configName);
		assertNotNull("We should be able to find the config \""+configName+"\"",stream);


		//Init resources
		RallyeConfig config = RallyeConfig.fromStream(stream);
		RallyeBinder.config = config;
		RallyeBinder.data = MockDataAdapter.getInstance();
		RallyeBinder.gameState = RallyeGameState.getInstance(RallyeBinder.data);
		
		//start server
		return new RallyeServer(config.getHostName(), config.getRestPort());
	}

}
