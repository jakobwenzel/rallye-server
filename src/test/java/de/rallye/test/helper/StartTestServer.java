/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rallye.test.helper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.admin.ServerConsole;
import de.rallye.exceptions.DataException;
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
		try {
			InputStream stream = StartTestServer.class.getResourceAsStream(configName);
			assertNotNull("We should be able to find the config \""+configName+"\"",stream);
			RallyeResources.init(stream, MockDataAdapter.getInstance());
		} catch (DataException e) {
			e.printStackTrace();
			fail("Resources could not be initialized"+ e);
			return null;
		}
		RallyeResources R = RallyeResources.getResources();
		
		//start server
		RallyeServer server = new RallyeServer(R.getConfig().getHostName(), R.getConfig().getRestPort());
		return server;
		
	}

}
