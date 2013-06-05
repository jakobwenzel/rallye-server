package de.rallye;

import java.util.HashMap;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;

public class RallyeResources {

	public final DataAdapter data;
	public final ImageRepository imgRepo;
	public final HashMap hashMap;
	
	public RallyeResources(DataAdapter data, ImageRepository imgRepo, HashMap hashmap) {
		this.data = data;
		this.imgRepo = imgRepo;
		this.hashMap = hashmap;
	}
}
