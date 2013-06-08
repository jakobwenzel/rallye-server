package de.rallye.push;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushMode;
import de.rallye.push.IPushAdapter;

public class PushService {
	
	private static Logger logger = LogManager.getLogger(PushService.class);
	
	private Map<Integer, IPushAdapter> pushAdapter = Collections.synchronizedMap(new HashMap<Integer, IPushAdapter>());

	public PushService(DataAdapter data) {
		
		try {
			for (PushMode p: data.getPushModes()) {
				pushAdapter.put(p.pushID, PushService.getPushAdapter(p.name));
			}
		} catch (DataException e) {
			logger.error(e);
		}
	}
	
	public static IPushAdapter getPushAdapter(String name) {
		switch (name) {
		case "gcm":
			return new GCMPushAdapter(RallyeConfig.GCM_API_KEY);
		default:
			return null;
		}
	}
}
