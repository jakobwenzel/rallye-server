package de.rallye;

import de.rallye.admin.ServerConsole;
import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;

public class StadtRallye {

	public static void main(String[] args) {
		
		String host = (args.length > 0 ? args[0]: RallyeConfig.host);
		
		
		
		DataAdapter data = RallyeConfig.getMySQLDataAdapter();//TODO: instantiate RallyeConfig, read Connection-Details from file
		ImageRepository imgRepo = RallyeConfig.getImageRepository();
		//TODO: read game config from file
		//TODO: create a Game Object
		
		RallyeServer server = new RallyeServer(host, RallyeConfig.port, data, imgRepo);
		
		ServerConsole console = new ServerConsole(RallyeConfig.consolePort);

		//wait until server should be closed by console command
		while (!console.accept());//TODO: move to Console Thread
		
		server.stopServer();
				
	}

}
