/**
 * 
 */
package de.rallye.pushService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javapns.communication.exceptions.KeystoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.pushService.resource.PushCommand;
import de.rallye.pushService.resource.PushServiceException;
import de.rallye.resource.DataHandler;

/**
 * @author Felix Huebner
 * @date 10.12.12
 * 
 */
public class PushService {

	private Logger logger = LogManager.getLogger(PushService.class.getName());

	public static final int DEVICE_GOOGLE = 10;
	public static final int DEVICE_APPLE = 20;

	private DataHandler data = null;
	private GCMPushService gcmPush = null;
	private APNPushService apnPush = null;

	/**
	 * 
	 * Constructor init the service and store the GCM_API_KEY
	 * 
	 * @param data
	 */
	public PushService(DataHandler data) {
		logger.entry();
		this.data = data;

		// create the google push service
		gcmPush = new GCMPushService(this.data.getGoogleCloudMessagingKey());

		// create the apple push service
		/*try {
			apnPush = new APNPushService(this.data.getAPN_keyStore(),
					this.data.getAPN_Password());
		} catch (KeystoreException e) {
			logger.catching(e);
		}*/
		logger.exit();
	}

	/**
	 * this method sends a push to all clients given in list, to tell them that
	 * they have to update chatroom given in chatroom
	 * 
	 * @param lst
	 *            list of clients to updatea
	 * @param chatroom
	 *            chatroom to update
	 * @throws PushServiceException
	 *             if a not valid DeviceType is found
	 */
	public void updateChatroom(Map<String, Integer> lst, int chatroom)
			throws PushServiceException {
		logger.entry();
		this.push(lst, PushCommand.CHATROOM_UPDATE, String.valueOf(chatroom));
		logger.exit();
	}
	
	public void startGame(Map<String, Integer> lst, long startTime) throws PushServiceException {
		logger.entry();
		this.push(lst, PushCommand.GAME_START, Long.toString(startTime));
		logger.exit();
		
	}

	/**
	 * send push notifications to the clients given in lst
	 * 
	 * @param lst
	 *            clients to send a push (String: ID, Integer: DeviceType)
	 * @param command
	 *            command type to send to the clients
	 * @param data
	 *            a small data to send to the clients
	 * @throws PushServiceException
	 */
	private void push(Map<String, Integer> lst, int command, String data)
			throws PushServiceException {
		logger.entry();
		LinkedList<String> gcmIDs = new LinkedList<String>();
		LinkedList<String> apnIDs = new LinkedList<String>();

		HashMap<String, String> changes = new HashMap<String, String>();

		// sort clients into arrays
		for (Entry<String, Integer> e : lst.entrySet()) {
			switch (e.getValue()) {
			case PushService.DEVICE_GOOGLE:
				gcmIDs.add(e.getKey());
				break;
			case PushService.DEVICE_APPLE:
				apnIDs.add(e.getKey());
				break;
			default:
				throw new PushServiceException("Invalid DeviceType found. ("
						+ e.getValue() + ")");
			}
		}

		// send the push to the clients - google cloud messaging
		gcmPush.push(gcmIDs, changes, command, data);
		if (!changes.isEmpty()) {
			this.fixClients(changes, PushService.DEVICE_GOOGLE);
		}

		// send the push to the clients - apple
		if (apnPush != null) {
			apnPush.push(apnIDs, changes, command, data);
			if (!changes.isEmpty()) {
				this.fixClients(changes, PushService.DEVICE_APPLE);
			}
		}

		logger.exit();
	}

	/**
	 * fix the uninstalled clients or changed ID clients
	 * 
	 * @param lst
	 *            list of changed to do (1. String is the current ID in the
	 *            system, 2.String is the new ID, if the 2.String is null the
	 *            client will be removed)
	 * @param clientType
	 *            client Type for the database access.
	 */
	private void fixClients(Map<String, String> lst, int clientType) {
		logger.entry(clientType);
		switch (clientType) {
		case PushService.DEVICE_GOOGLE:
			for (Entry<String, String> e : lst.entrySet()) {

				//logger.debug("Client: ID1:" + e.getKey() + " ID2:" + e.getValue());

				if (e.getValue() == null) {
					// remove device
					logger.info("remove device: " + e.getKey());
					// TODO comment in to enable remove!!!
					// this.data.removeUser(e.getKey());
				} else {
					// update device
					logger.info("update Device: from:" + e.getKey() + " to:"
							+ e.getValue());
					this.data.updateUser(e.getKey(), e.getValue());
				}
			}
			break;
		case PushService.DEVICE_APPLE:
			for (Entry<String, String> e : lst.entrySet()) {

				logger.debug("Client: ID1:" + e.getKey() + " ID2:" + e.getValue());

				if (e.getValue() == null) {
					// remove device
					logger.info("remove device: " + e.getKey());
					// TODO comment in to enable remove!!!
					// this.data.removeUser(e.getKey());
				}
			}
			break;
		}
		lst.clear();
		logger.exit();
	}

}
