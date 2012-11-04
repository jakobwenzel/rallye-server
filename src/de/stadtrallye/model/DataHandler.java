package de.stadtrallye.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class DataHandler {

	private static Connection sqlCon;
	
	//private String uri = "hajoschja.de";
	private String uri = "localhost";
	private int Port = 10101;
	private final String GCM_API_KEY = "AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0";

	/**
	 * constructor
	 */
	public DataHandler() {
		// create a new connection if no connection exists
		if (DataHandler.sqlCon == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				sqlCon = java.sql.DriverManager.getConnection("jdbc:mysql://hajoschja.de/rallye?user=felix&password=andro-rallye");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
	}
	
	/**
	 * this method update all Clients in all Chatrooms given in the HashSet
	 * @param chatrooms
	 * @author Felix HŸbner
	 */
	public void updateDevices(HashSet<Integer> chatrooms) {
		StringBuilder str = new StringBuilder();
		str.append("SELECT distinct c.clientID FROM ry_chatrooms_groups as cg, ry_clients as c WHERE c.groupID = cg.groupID AND (");
		for (Integer i : chatrooms) {
			str.append("cg.chatroomID = "+i.intValue()+" OR ");
		}
		// cut the last OR
		str.delete(str.length()-3, str.length());
		str.append(")");
		
		//DEBUG
		System.out.println(str.toString());
		
		
		try {
			ResultSet rs = DataHandler.sqlCon.createStatement().executeQuery(str.toString());
			Boolean empty = rs.first();
			
			while(empty) {
				// update client
				//DEBUG
				System.out.println("Update Client with id: "+rs.getInt(1));
				
				// jump to next client
				empty = rs.next();
			}
			
			
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	/**
	 * @return the sqlCon
	 */
	protected Connection getSqlCon() {
		return DataHandler.sqlCon;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return Port;
	}
	
	/**
	 * Google GCM, snippet
	 */
	public void push(String client,String type, String value) {
		Sender sender = new Sender(GCM_API_KEY);
		Message msg = new Message.Builder()
	    .collapseKey("update")
	    .timeToLive(3)
	    .addData(type, value)
	    .build();
		try {
			Result res = sender.send(msg, client, 3);// RegIds
			
			if (res.getMessageId() != null) {
				String canonicalRegId = res.getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					// same device has more than on registration ID: update
					// database
				}
			} else {
				String error = res.getErrorCodeName();
				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
					// application has been removed from device - unregister
					// database
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO make throw exceptions if user has changed regID, or if user not available anymore
	}
}
