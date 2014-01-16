/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rallye.test.helper;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import de.rallye.RallyeServer;
import de.rallye.config.RallyeConfig;
import de.rallye.injection.RallyeBinder;
import de.rallye.model.structures.RallyeGameState;
import de.rallye.test.db.MockDataAdapter;
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
