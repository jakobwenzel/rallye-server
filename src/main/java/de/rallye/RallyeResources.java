package de.rallye;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.GameState;
import de.rallye.push.PushService;

public class RallyeResources {

	public final DataAdapter data;
	public final ImageRepository imgRepo;
	public final Map<String, ChatPictureLink> hashMap = Collections.synchronizedMap(new HashMap<String, ChatPictureLink>());
	public final PushService push;
	public GameState gameState;
	
	private RallyeResources(DataAdapter data, ImageRepository imgRepo, PushService push) {
		this.data = data;
		this.imgRepo = imgRepo;
		this.push = push;
		
		gameState = new GameState(data);  
	}

	public GameState getGameState() {
		// TODO Auto-generated method stub
		return gameState;
	}
	
	private static RallyeResources resources = null;

	public static void init() {
		if (resources!=null) return; //We only want to init once
		
		DataAdapter data;
		try {
			data = RallyeConfig.getMySQLDataAdapter();
			// TODO: instantiate RallyeConfig, read Connection-Details from file
			ImageRepository imgRepo = RallyeConfig.getImageRepository();
			// TODO: read game config from file
			// TODO: create a Game Object
			PushService push = new PushService(data);

			resources = new RallyeResources(data, imgRepo, push);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static RallyeResources getResources() {
		// TODO Auto-generated method stub
		return resources;
	}
}
