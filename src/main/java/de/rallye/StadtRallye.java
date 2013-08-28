package de.rallye;

import de.rallye.admin.ServerConsole;


public class StadtRallye {
	public static void main(String[] args) {
		//Init resources
		RallyeResources.init();
		
		//start server

		String host = (args.length > 0 ? args[0] : RallyeConfig.getHostName());
		RallyeServer server = new RallyeServer(host, RallyeConfig.getRestPort());

		ServerConsole console = new ServerConsole(RallyeConfig.getConsolePort(), server);
		console.start();
	}
}
