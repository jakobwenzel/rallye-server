package de.rallye.push;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushEntity;
import de.rallye.model.structures.UserInternal;
import de.rallye.model.structures.PushEntity.Type;

public class GCMPushAdapter implements IPushAdapter {
	
	private final Logger logger = LogManager.getLogger(GCMPushAdapter.class);
	private static Executor threadPool = Executors.newCachedThreadPool();
	
	private static final int RETRIES = 3;
	private static final int TTL = 60;
	private static final int MAX_IDS = 1000;
	
	private Sender sender;
	private DataAdapter data;
	
	
	public GCMPushAdapter(String gcmKey, DataAdapter data) {
		this.sender = new Sender(gcmKey);
		this.data = data;
	}
	
	@Override
	public void push(List<UserInternal> users, String payload, Type type) {
		
		Message msg = new Message.Builder().timeToLive(TTL).addData(PushEntity.TYPE, type.toString())
				.addData(PushEntity.PAYLOAD, payload).build();
		
		int max = (users.size() > MAX_IDS)? MAX_IDS : users.size();
		int count = 0;
		
		ArrayList<String> partialList = new ArrayList<String>(max);
		for (UserInternal u: users) {
			partialList.add(u.pushID);
			count++;
			
			if (count == max) {
				pushAsync(partialList, msg);
				if (max < MAX_IDS) {
					partialList = new ArrayList<String>(max);
					count = 0;
				}
			}
		}
	}
	
	private void pushAsync(final List<String> users, final Message msg) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				HashMap<String, String> changes = new HashMap<String, String>();
				
				MulticastResult res = null;
				try {
					logger.debug("pushing: {} to {}", msg, users);
					res = sender.send(msg, users, RETRIES);
				} catch (IOException e) {
					logger.fatal("Google Cloud Messaging failed", e);
				}
				
				if (res.getCanonicalIds() > 0 || res.getFailure() > 0) {
					
					List<Result> results = res.getResults();
					
					Result r = null;
					for (int i=results.size()-1; i >= 0; i--) {
						r = results.get(i);
						
						if (r.getMessageId() != null) {
							String canonicalRegId = r.getCanonicalRegistrationId();
							
							if (canonicalRegId != null) {
								logger.warn("deviceID has changed: {}", canonicalRegId);
								changes.put(users.get(i), canonicalRegId);
							}
						} else {
							String error = r.getErrorCodeName();
							if (error.equals(Constants.ERROR_NOT_REGISTERED) || error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
								logger.warn("client has removed app from device");
								changes.put(users.get(i), null);
							} else
								logger.error(error);
						}
					}
				}
				
				if (!changes.isEmpty()) {
					try {
						logger.debug("PushIDs changed: {}"+ changes);
						data.updatePushIds(changes);
					} catch (DataException e) {
						logger.error("Failed to update changed pushIDs", e);
					}
				}
			}
		});
	}
}
