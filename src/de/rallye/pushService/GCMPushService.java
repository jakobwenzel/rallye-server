/**
 * 
 */
package de.rallye.pushService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import de.rallye.pushService.resource.PushServiceInterface;
import de.rallye.resource.DataHandler;

/**
 * @author Felix Huebner
 * @date 10.12.12
 * 
 */
public class GCMPushService implements PushServiceInterface {

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
		this.GCM_API_KEY = gcmKey;
	}

	/**
	 * Google GCM, single client push
	 */
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

	/**
	 * Google GCM, multi client push
	 * 
	 * @param ar
	 * @param type
	 * @param value
	 */
	public void push(List<String> lst, Map<String, String> lstChages, int type,
			String value) {
		logger.entry();
		Sender sender = new Sender(GCM_API_KEY);
		Message msg = new Message.Builder().collapseKey("update").timeToLive(3)
				.addData("t", String.valueOf(type)).addData("d", value).build();
		try {
			MulticastResult res = sender.send(msg, lst, 3);
			if (res.getCanonicalIds() > 0 || res.getFailure() > 0) {
				for (Result r : res.getResults()) {
					if (r.getMessageId() != null) {
						String canonicalRegId = r.getCanonicalRegistrationId();
						if (canonicalRegId != null) {
							logger.info("deviceID has changed! id: "
									+ canonicalRegId);
							lstChages.put(r.getMessageId(), canonicalRegId);
						}
					} else {
						String error = r.getErrorCodeName();
						logger.trace(error);
						if (error.equals(Constants.ERROR_NOT_REGISTERED)
								|| error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
							logger.info("client has removed app from device: "
									+ r.getMessageId());
							lstChages.put(r.getMessageId(), null);
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
