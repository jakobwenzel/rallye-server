package de.rallye;

import de.rallye.admin.ServerConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StadtRallye {

	private static Logger logger = LogManager.getLogger(StadtRallye.class);

	public static void main(String[] args) {
		logger.info("Starting RallyeServer");

		//Init resources
		RallyeResources.init();
		RallyeResources R = RallyeResources.getResources();
		
		//start server
		String host = (args.length > 0 ? args[0] : R.getConfig().getHostName());
		RallyeServer server = new RallyeServer(host, R.getConfig().getRestPort());

		ServerConsole console = new ServerConsole(R.getConfig().getConsolePort(), server);
		console.start();
	}
}
