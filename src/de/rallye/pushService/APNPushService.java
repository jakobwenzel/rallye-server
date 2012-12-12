/**
 * 
 */
package de.rallye.pushService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.Payload;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.test.NotificationTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import de.rallye.pushService.resource.IPushService;

/**
 * @author Felix Huebner
 * @date 11.12.2012
 * 
 */
public class APNPushService implements IPushService {
	private Logger logger = LogManager
			.getLogger(APNPushService.class.getName());
	private static String password;
	private static boolean production = false;
	private static int workingThreads = 2;
	private static Object keyStore;

	/**
	 * @throws KeystoreException
	 *             if the keystore is not vaild
	 * 
	 */
	public APNPushService(Object keystore, String password) throws KeystoreException {
		logger.entry();
		
		// store the password
		APNPushService.password = password;

		// store the keyStore path
		APNPushService.keyStore = keystore;
		
		NotificationTest.verifyKeystore(keystore, password, production);

		logger.exit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.pushService.resource.IPushService#push(java.util.List,
	 * java.util.Map, int, java.lang.String)
	 */
	@Override
	public void push(List<String> lst, Map<String, String> lstChanges,
			int type, String value) {
		logger.entry();
		try {

			// create Payload
			JSONObject o = new JSONObject();
			o.put("t", String.valueOf(type));
			o.put("d", value);
			Payload payload = PushNotificationPayload.fromJSON(o.toString());

			// create Devices
			List<Device> deviceList = new ArrayList<Device>(lst.size());
			for (String s : lst) {
				deviceList.add(new BasicDevice(s));
			}

			// process notifications
			List<PushedNotification> notifications = Push.payload(payload,
					APNPushService.keyStore, APNPushService.password,
					APNPushService.production, APNPushService.workingThreads,deviceList);

			// debug result
			for (PushedNotification pn : notifications) {
				if (pn.isSuccessful()) {
					logger.info("Sucess Response from device: "
							+ pn.getDevice().getDeviceId());
				} else {
					logger.trace("Failed Response from device: "
							+ pn.getDevice().getDeviceId()
							+ " ResponseMessage: "
							+ pn.getResponse().getMessage());
				}
			}

		} catch (JSONException e) {
			logger.catching(e);
		} catch (Exception e) {
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
	public void push(String client, Map<String, String> lstChanges, int type,
			String value) {
		logger.entry();
		try {
			// create Payload
			JSONObject o = new JSONObject();
			o.put("t", String.valueOf(type));
			o.put("d", value);
			Payload payload = PushNotificationPayload.fromJSON(o.toString());

			// process notifications
			List<PushedNotification> notifications = Push.payload(payload,
					APNPushService.keyStore, APNPushService.password,
					APNPushService.production, client);

			// debug result
			for (PushedNotification pn : notifications) {
				if (pn.isSuccessful()) {
					logger.info("Sucess Response from device: "
							+ pn.getDevice().getDeviceId());
				} else {
					logger.trace("Failed Response from device: "
							+ pn.getDevice().getDeviceId()
							+ " ResponseMessage: "
							+ pn.getResponse().getMessage());
					if (pn.getResponse().isValidErrorMessage()) {
						logger.trace("error status code: "+pn.getResponse().getStatus());
						logger.trace("error status message:"+pn.getResponse().getMessage());
						lstChanges.put(pn.getDevice().getDeviceId(), null);
					}
					
				}
			}

		} catch (JSONException e) {
			logger.catching(e);
		} catch (CommunicationException e) {
			logger.catching(e);
		} catch (KeystoreException e) {
			logger.catching(e);
		}

		logger.exit();
	}

}
