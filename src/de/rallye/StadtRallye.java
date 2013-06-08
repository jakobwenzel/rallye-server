package de.rallye;

import de.rallye.admin.ServerConsole;
import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.push.PushService;

public class StadtRallye {

	public static void main(String[] args) {
		
		String host = (args.length > 0 ? args[0]: RallyeConfig.HOST);
		
		
		
		DataAdapter data = RallyeConfig.getMySQLDataAdapter();//TODO: instantiate RallyeConfig, read Connection-Details from file
		ImageRepository imgRepo = RallyeConfig.getImageRepository();
		//TODO: read game config from file
		//TODO: create a Game Object
		PushService push = new PushService(data);
		
		RallyeServer server = new RallyeServer(host, RallyeConfig.PORT, data, imgRepo, push);
		
		ServerConsole console = new ServerConsole(RallyeConfig.CONSOLE_PORT, server);
		
	}
}
