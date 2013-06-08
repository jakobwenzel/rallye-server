package de.rallye;

import java.util.Map;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.push.PushService;

public class RallyeResources {

	public final DataAdapter data;
	public final ImageRepository imgRepo;
	public final Map<String, ChatPictureLink> hashMap;
	public final PushService push;
	
	public RallyeResources(DataAdapter data, ImageRepository imgRepo, Map<String, ChatPictureLink> hashmap, PushService push) {
		this.data = data;
		this.imgRepo = imgRepo;
		this.hashMap = hashmap;
		this.push = push;
	}
}
