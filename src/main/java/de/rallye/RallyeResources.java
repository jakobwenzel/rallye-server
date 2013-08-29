package de.rallye;

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
	
	public RallyeResources(DataAdapter data, ImageRepository imgRepo, PushService push) {
		this.data = data;
		this.imgRepo = imgRepo;
		this.push = push;
		
		gameState = new GameState(data);  
	}

	public GameState getGameState() {
		// TODO Auto-generated method stub
		return gameState;
	}
}
