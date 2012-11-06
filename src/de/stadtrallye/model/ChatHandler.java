package de.stadtrallye.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashSet;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.stadtrallye.resource.exceptions.SQLHandlerException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class ChatHandler {
	/**
	 * constructor
	 * 
	 * @param dataHandler
	 */
	public ChatHandler() {
	}

	public static HashSet<Integer> setNewChatEntry(DataHandler dh, byte[] pic, String userID, String message) throws SQLHandlerException {
		HashSet<Integer> chatrooms = new HashSet<Integer>();
		Connection con = dh.getSqlCon();
		PreparedStatement stmt = null;
		Savepoint sPoint = null;
		

		try {
			// disable auto commit
			con.setAutoCommit(false);

			// create savePoint for a rollback if an error occurs
			sPoint = con.setSavepoint();
			if (message != null && !message.isEmpty()) {
				// create prepared statement for insert message into ry_messages
				stmt = con.prepareStatement("INSERT INTO ry_messages (message) VALUES (?)");
				stmt.setString(1, message);
				stmt.execute();
				// debug
				System.out.println("Warnings insert Message: " + stmt.getWarnings());
				stmt.close();
			}

			if (pic.length != 0) {
				// create prepared statement for insert message into ry_messages
				stmt = con.prepareStatement("INSERT INTO ry_pictures (picture) VALUES (?)");
				stmt.setBytes(1, pic);
				stmt.execute();
				// debug
				System.out.println("Warnings insert Picture: " + stmt.getWarnings());
				stmt.close();
			}

			if ((message != null && !message.isEmpty()) || pic.length != 0) {
				// get all chatroom where we have to post this entry
				ResultSet rs = con.createStatement().executeQuery(
						"SELECT cg.chatroomID FROM ry_chatrooms_groups as cg, ry_clients as c WHERE c.gcmRegID = '"+userID+"' AND c.groupID = cg.groupID");
				Boolean available = rs.first();
				if (!available) {
					con.rollback(sPoint);
					con.releaseSavepoint(sPoint);
					throw new SQLHandlerException("Add entry to database failed!: User unknown!");
				}
				while (available) {

					// create new chat entry into ry_chats
					stmt = con.prepareStatement("INSERT INTO ry_chats (timestamp,groupID,messageID,pictureID,chatroomID) VALUES (" + "now(),"
							+ "(SELECT groupID FROM ry_clients WHERE gcmRegID = '"+userID+"'),"
							+ ((message != null && !message.isEmpty()) ? "(SELECT MAX(messageID) FROM ry_messages)," : "NULL,")
							+ (pic.length != 0 ? "(SELECT MAX(pictureID) FROM ry_pictures)," : "NULL,") + rs.getInt(1) +")");
					stmt.execute();
					// debug
					System.out.println("Warnings insert ChatEntry: " + stmt.getWarnings());
					stmt.close();
					// save chatroom number in array
					chatrooms.add(rs.getInt(1));
					
					// move result element-curser 1 element further
					available = rs.next();

				}
				rs.close();
			} else {
				con.rollback(sPoint);
				con.releaseSavepoint(sPoint);
				throw new SQLHandlerException("Add entry to database failed!: Unknown Error!");
			}

			// apply changes in database
			con.commit();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

			// release savePoint
			try {
				con.rollback(sPoint);
				con.releaseSavepoint(sPoint);
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new SQLHandlerException(e1);
		}
		return chatrooms;
	}
	
	
	public static JSONArray getChatEntries(DataHandler dh, String user, int timestamp) throws SQLHandlerException, JSONException {
		Connection con = dh.getSqlCon();
		JSONArray lst = new JSONArray();
		JSONObject o = null;
		
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT distinct UNIX_TIMESTAMP(c.timestamp) as timestamp,c.groupID,c.pictureID,c.messageID,(SELECT message FROM ry_messages WHERE messageID = c.messageID) as message " 
					+ "FROM ry_chats as c, ry_messages as m, ry_chatrooms_groups as cg, ry_clients as cl "
					+ "WHERE cl.groupID = cg.groupID AND cg.chatroomID = c.chatroomID AND c.timestamp >= FROM_UNIXTIME(?) AND cl.gcmRegID = ? ORDER BY c.timestamp ASC");
			stmt.setInt(1, timestamp);
			stmt.setString(2, user);
			ResultSet rs = stmt.executeQuery();
			
			Boolean available = rs.first();
			while (available) {
				o = new JSONObject();
				o.put("timestamp", rs.getInt("timestamp"));
				o.put("groupID",rs.getInt("groupID"));
				if (rs.getInt("messageID") != 0) {
				o.put("message", rs.getString("message"));
				}
				if (rs.getInt("pictureID") != 0) {
					o.put("picture", rs.getInt("pictureID"));
				}
				//add object to array
				lst.put(o);
				
				//DEBUG
				System.out.println(o.toString());
				
				// jump to next return row
				available = rs.next();
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SQLHandlerException(e.getMessage());
		}
		
		return lst;
		
	}
}
