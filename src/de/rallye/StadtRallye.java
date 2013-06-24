package de.rallye;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.rallye.admin.ServerConsole;
import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.push.PushService;

public class StadtRallye {

	public static void main(String[] args) {

		String host = (args.length > 0 ? args[0] : RallyeConfig.HOST);

		DataAdapter data;
		try {
			
			data = RallyeConfig.getMySQLDataAdapter();
			// TODO: instantiate RallyeConfig, read Connection-Details from file
			ImageRepository imgRepo = RallyeConfig.getImageRepository();
			// TODO: read game config from file
			// TODO: create a Game Object
			PushService push = new PushService(data);

			RallyeResources resources = new RallyeResources(data, imgRepo, push);

			RallyeServer server = new RallyeServer(host, RallyeConfig.PORT,
					resources);

			ServerConsole console = new ServerConsole(
					RallyeConfig.CONSOLE_PORT, server);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
