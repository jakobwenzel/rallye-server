package de.rallye;

import de.rallye.admin.ServerConsole;


public class StadtRallye {
	public static void main(String[] args) {
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
