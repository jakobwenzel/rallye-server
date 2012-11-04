package de.stadtrallye.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashSet;
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
			if (!message.isEmpty()) {
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

			if (!message.isEmpty() || pic.length != 0) {
				// get all chatroom where we have to post this entry
				ResultSet rs = con.createStatement().executeQuery(
						"SELECT cg.chatroomID FROM ry_chatrooms_groups as cg, ry_clients as c WHERE c.gcmRegID = '"+userID+"' AND c.groupID = cg.groupID");
				Boolean empty = rs.first();
				
				while (empty) {

					// create new chat entry into ry_chats
					stmt = con.prepareStatement("INSERT INTO ry_chats (timestamp,groupID,messageID,pictureID,chatroomID) VALUES (" + "now(),"
							+ "(SELECT groupID FROM ry_clients WHERE gcmRegID = '"+userID+"'),"
							+ (!message.isEmpty() ? "(SELECT MAX(messageID) FROM ry_messages)," : "NULL,")
							+ (pic.length != 0 ? "(SELECT MAX(pictureID) FROM ry_pictures)," : "NULL,") + rs.getInt(1) +")");
					stmt.execute();
					// debug
					System.out.println("Warnings insert ChatEntry: " + stmt.getWarnings());
					stmt.close();
					// save chatroom number in array
					chatrooms.add(rs.getInt(1));
					
					// move result element-curser 1 element further
					empty = rs.next();

				}
				rs.close();
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
}
