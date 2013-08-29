package de.rallye;

import de.rallye.admin.ServerConsole;
import de.rallye.exceptions.DataException;
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

	private static Logger logger = LogManager.getLogger(StadtRallye.class);

	public static void main(String[] args) {
		logger.info("Starting RallyeServer");

		//Init resources
		try {
			RallyeResources.init();
		} catch (DataException e) {
			logger.error("Resources could not be initialized", e);
			return;
		}
		RallyeResources R = RallyeResources.getResources();
		
		//start server
		String host = (args.length > 0 ? args[0] : R.getConfig().getHostName());
		RallyeServer server = new RallyeServer(host, R.getConfig().getRestPort());

		ServerConsole console = new ServerConsole(R.getConfig().getConsolePort(), server);
		console.start();
	}
}
