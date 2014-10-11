/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.push;

import com.google.android.gcm.server.*;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushEntity;
import de.rallye.model.structures.PushEntity.Type;
import de.rallye.model.structures.UserInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GCMPushAdapter implements IPushAdapter {
	
	private final Logger logger = LogManager.getLogger(GCMPushAdapter.class);
	private static final Executor threadPool = Executors.newCachedThreadPool();
	
	private static final int RETRIES = 3;
	private static final int TTL = 60;
	private static final int MAX_IDS = 1000;
	
	private final Sender sender;
	private final IDataAdapter data;


	public GCMPushAdapter(String gcmKey, IDataAdapter data) {
		this.sender = new Sender(gcmKey);
		this.data = data;
	}
	
	@Override
	public void push(List<UserInternal> users, String payload, Type type) {
		
		Message msg = new Message.Builder().timeToLive(TTL).addData(PushEntity.TYPE, type.toString())
				.addData(PushEntity.PAYLOAD, payload).build();
		
		int max = (users.size() > MAX_IDS)? MAX_IDS : users.size();
		int count = 0;
		
		ArrayList<String> partialList = new ArrayList<>(max);
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
