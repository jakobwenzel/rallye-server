/**
 * 
 */
package de.rallye.push;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


/**
 * @author Felix Huebner
 * @date 10.12.12
 * 
 */
@Deprecated
public class GCMPushService implements IPushService {

	private Logger logger = LogManager
			.getLogger(GCMPushService.class.getName());
	private String GCM_API_KEY = null;

	/**
	 * 
	 * Constructor init the service and store the GCM_API_KEY
	 * 
	 * @param data
	 */
	public GCMPushService(String gcmKey) {
		logger.entry();
		this.GCM_API_KEY = gcmKey;
		logger.exit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.pushService.resource.IPushService#push(java.util.List,
	 * java.util.Map, int, java.lang.String)
	 */
	@Override
	public void push(String client, Map<String, String> lstChages, int type,
			String value) {
		logger.entry();
		Sender sender = new Sender(GCM_API_KEY);
		Message msg = new Message.Builder().collapseKey("update").timeToLive(3)
				.addData(String.valueOf(type), value).build();
		try {
			Result res = sender.send(msg, client, 3);
			if (res.getMessageId() != null) {
				String canonicalRegId = res.getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					logger.info("deviceID has changed! id: " + canonicalRegId);
					lstChages.put(client, canonicalRegId);
				}
			} else {
				String error = res.getErrorCodeName();
				logger.trace(error);
				if (error.equals(Constants.ERROR_NOT_REGISTERED)
						|| error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
					logger.info("client has removed app from device: " + client);
					lstChages.put(client, null);
				}
			}
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.exit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.pushService.resource.IPushService#push(java.lang.String,
	 * java.util.Map, int, java.lang.String)
	 */
	@Override
	public void push(List<String> lst, Map<String, String> lstChages, int type,
			String value) {
		logger.entry();
		Sender sender = new Sender(GCM_API_KEY);
		Message msg = new Message.Builder().collapseKey("update").timeToLive(3)
				.addData("t", String.valueOf(type)).addData("d", value).build();
		try {
			MulticastResult res = sender.send(msg, lst, 3);
			if (res.getCanonicalIds() > 0 || res.getFailure() > 0) {
				Result r = null;
				for (int i = 0; i < res.getResults().size(); i++) {
					r = res.getResults().get(i);
					if (r.getMessageId() != null) {
						String canonicalRegId = r.getCanonicalRegistrationId();
						if (canonicalRegId != null) {
							logger.info("deviceID has changed! id: "
									+ canonicalRegId);
							lstChages.put(lst.get(i), canonicalRegId);
						}
					} else {
						String error = r.getErrorCodeName();
						//logger.trace(error);
						if (error.equals(Constants.ERROR_NOT_REGISTERED)
								|| error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
							logger.info("client has removed app from device: "
									+ lst.get(i));
							lstChages.put(lst.get(i), null);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.exit();
	}
}
