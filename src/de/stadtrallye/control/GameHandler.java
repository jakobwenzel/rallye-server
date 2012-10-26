package de.stadtrallye.control;

import java.io.IOException;

import de.stadtrallye.model.DataHandler;
import com.google.android.gcm.server.*;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class GameHandler {
	DataHandler data;
	ClientListener listener;
	
	public GameHandler() {
	// create and init new DataHander
	data = new DataHandler();
	
	//create and init new ClientListener,
	listener = new ClientListener(10101);
	}
	
	/**
	 * Google GCM, snippet
	 */
	public void push() {
		Sender sender = new Sender("AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0");
		Message msg = new Message.Builder().build();
		try {
			Result res = sender.send(msg, "", 3);//RegIds
			
			if (res.getMessageId() != null) {
				 String canonicalRegId = res.getCanonicalRegistrationId();
				 if (canonicalRegId != null) {
				   // same device has more than on registration ID: update database
				 }
				} else {
				 String error = res.getErrorCodeName();
				 if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				   // application has been removed from device - unregister database
				 }
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
