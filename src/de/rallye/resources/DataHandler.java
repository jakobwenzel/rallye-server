package de.rallye.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.rallye.db.Ry;
import de.rallye.exceptions.SQLHandlerException;
import de.rallye.exceptions.WebAppExcept;
import de.rallye.json.Tags;
import de.rallye.pushService.PushService;
import de.rallye.pushService.resource.PushServiceException;

/**
 * @author Felix H�bner
 * @version 1.0
 * 
 */
public class DataHandler {
	protected static ComboPooledDataSource dataSource;
	private static int httpPort = 10101;
	private static int consolePort = 10100;
	private static String GCM_API_KEY = "AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0";
	private static String APN_Password = "";
	private static String APN_keyStore = "config/keystore.p12";
	private static TemporaryCache<Long, byte[]> uploadedImages;
	private static BlobStore blobStore;
	private static PushService pushService;
	private static GameControlConfig gcconfig;
	private static int thump_width = 96;
	private static int thump_height = 96;
	private static int small_width = 1280;
	private static int small_height = 1280;
	private Logger logger = LogManager.getLogger(DataHandler.class.getName());

	private static DataHandler instance;
	
	public static DataHandler getInstance() {
		if (instance != null) {
			return instance;
		} else {
			return instance = new DataHandler();
		}
	}
	
	/**
	 * 
	 */
	private DataHandler() {
		logger.entry();
		// create a new instance of the TemporaryCache if the pointer is null
		if (uploadedImages == null) {
			uploadedImages = new TemporaryCache<Long, byte[]>();
			uploadedImages.initialize(5 * 60); // set validTime to 5 minutes
		}

		// create a new instance of the BlobStore if the pointer is null
		if (blobStore == null) {
			blobStore = new BlobStore();
			blobStore.initialize(new File("").getAbsolutePath(), "imageCache",
					25, (3 * 1024 * 1024));
		}

		// create a new instance of the PushService if the pointer is null
		if (pushService == null) {
			pushService = new PushService(this);
		}

		// create a new instance of the TemporaryCahce if the pointer is null
		if (gcconfig == null) {
			gcconfig = new GameControlConfig(this);
		}

		logger.exit();
	}
	
	
	public Response getGroups() {
		logger.entry();
		
		Statement st = null;
		Connection con = null;
		JSONArray lst = new JSONArray();
		ResultSet rs = null;

		try {
			con = this.getSqlCon();
			st = con.createStatement();
//			rs = st.executeQuery("SELECT "+ strStr(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION) +" FROM "+ Ry.Groups.TABLE);

			while (rs.next()) {

				JSONObject j = new JSONObject();

				j.put(Tags.GROUP_ID, rs.getInt(1))
					.put(Tags.NAME, rs.getString(2))
					.put(Tags.DESCRIPTION, rs.getString(3));

				lst.put(j);
			}
			rs.close();
			st.close();
			con.close();

			return Response.ok(lst).build();
		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			return Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			return Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	/**
	 * this method update all Clients in all Chatrooms given in the HashSet
	 * 
	 * @param chatroom
	 * @author Felix H�bner
	 */
	public void updateDevices(int chatroom) {
		logger.entry();
		// HashSet<String> lst = new HashSet<String>();
		// LinkedList<String> lst = new LinkedList<String>();
		HashMap<String, Integer> lst = new HashMap<String, Integer>();
		try {
			ResultSet rs = this
					.getSqlCon()
					.createStatement()
					.executeQuery(
							"SELECT c.gcmRegID FROM ry_chatrooms_groups as cg RIGHT JOIN ry_clients as c ON(c.groupID=cg.groupID) WHERE cg.chatroomID = "
									+ chatroom);
			Boolean empty = rs.first();

			while (empty) {
				lst.put(rs.getString(1), PushService.DEVICE_GOOGLE);
				// update client
				// DEBUG
				logger.trace("Update Client with id: " + rs.getString(1));

				// jump to next client
				empty = rs.next();
			}
			// push the info to the clients
			pushService.updateChatroom(lst, chatroom);

		} catch (SQLException e) {
			logger.catching(e);
		} catch (PushServiceException e) {
			logger.catching(e);
		}
		logger.info("GCM Devices (" + lst.size() + ") updated in chatroom:"
				+ chatroom);

		logger.exit();
	}

	public String getModelStatus() {
		StringBuilder str = new StringBuilder();
		for (String s : blobStore.getStatus().split("\n")) {
			str.append("BlobStore Status: ").append(s).append("\n");
		}

		for (String s : uploadedImages.getStatus().split("\n")) {
			str.append("TemporaryCache Status: ").append(s).append("\n");
		}
		try {
			str.append("DataSource Status: NumThreads: ")
					.append(dataSource.getThreadPoolSize())
					.append(" Idle/Active/Pending: ")
					.append(dataSource.getThreadPoolNumIdleThreads())
					.append("/")
					.append(dataSource.getThreadPoolNumActiveThreads())
					.append("/")
					.append(dataSource.getThreadPoolNumTasksPending())
					.append("\n");
		} catch (SQLException e) {
			logger.catching(e);
		}
		for (String s : gcconfig.getStatus().split("\n")) {
			str.append("GameControlConfig: ").append(s).append("\n");
		}
		return str.toString();
	}

	/**
	 * @return the sqlCon
	 * @throws SQLException
	 */
	protected Connection getSqlCon() throws SQLException {
		return DataHandler.dataSource.getConnection();
	}

	/**
	 * @param dataSource
	 *            the dataSource to set
	 */
	public static void setDataSource(ComboPooledDataSource dataSource) {
		DataHandler.dataSource = dataSource;
	}

	/**
	 * get the url
	 * 
	 * @return the url of the host (can be a ip-address), if not available
	 *         "localhost" will be returned
	 */
	public String getUri() {
		// return uri;
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}

	/**
	 * @return the httpPort
	 */
	public int getHttpPort() {
		return httpPort;
	}

	/**
	 * @return the ConsolePort
	 */
	public int getConsolePort() {
		return consolePort;
	}

	/**
	 * @return the GCM_API_KEY
	 */
	public String getGoogleCloudMessagingKey() {
		return GCM_API_KEY;
	}

	/**
	 * @return the aPN_Password
	 */
	public String getAPN_Password() {
		return APN_Password;
	}

	/**
	 * @return the aPN_keyStore
	 */
	public String getAPN_keyStore() {
		return APN_keyStore;
	}

	/**
	 * @return the gcconfig
	 * @category getter
	 */
	public GameControlConfig getGcconfig() {
		return gcconfig;
	}

	// ==================================================================//
	// Chat Commands
	// ==================================================================//

	/**
	 * this method set a new chet entry with the parameters given in the
	 * JSONObject req
	 * 
	 * @param req
	 *            values for this method as JSONObject with the parameters: <li>
	 *            - 'gcm' as String Google Cloud Messaging ID used as unique ID
	 *            for the client</li><li>- 'chatroom' as int</li><li>- 'mess' as
	 *            String (can be null) message to add to a chatroom</li><li>-
	 *            'pic' as Long (can be null) pictureID given be the method
	 *            setPicture()</li>
	 * @return HTTP 201 if the Add was successful or 500 if an error occurs
	 * @author Felix H�bner
	 */
	public Response setNewChatEntry(JSONObject req) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Savepoint sPoint = null;
		Response r = Response.ok().build();

		// set default values for variables from JSONObject
		int chatroom = -1;
		String gcm = null;
		String message = null;
		Long picID = null;

		int picPos = -1;
		int mesPos = -1;

		try {
			// check if field gcm exists
			if (!req.isNull("gcm")) {
				gcm = req.getString("gcm");
			} else {
				throw new WebAppExcept("field 'gcm' not found!", 400);
			}

			// check if field chatroom exists
			if (!req.isNull("chatroom")) {
				chatroom = req.getInt("chatroom");
			} else {
				throw new WebAppExcept("field 'chatroom' not found!", 400);
			}

			// check if field mess exists
			if (!req.isNull("mess")) {
				message = req.getString("mess");
			} else {
				if (!req.has("mess")) {
					throw new WebAppExcept("field 'mess' not found!", 400);
				}
				message = null;
			}

			// check if field mess exists
			if (!req.isNull("pic")) {
				picID = req.getLong("pic");
			} else {
				if (!req.has("pic")) {
					throw new WebAppExcept("field 'pic' not found!", 400);
				}
				picID = null;
			}

			con = this.getSqlCon();

			// disable auto commit
			con.setAutoCommit(false);

			// create savePoint for a rollback if an error occurs
			sPoint = con.setSavepoint();
			if (message != null && !message.isEmpty()) {

				// create prepared statement for insert message into ry_messages
				stmt = con
						.prepareStatement("INSERT INTO ry_messages (message) VALUES (?)");
				stmt.setString(1, message);
				stmt.execute();

				// get next autoIncrement value from database
				rs = con.createStatement().executeQuery(
						"SELECT LAST_INSERT_ID() FROM ry_messages");
				if (!rs.first()) {
					throw new SQLHandlerException(
							"Error while reading table entries.");
				}

				mesPos = rs.getInt(1);

				// debug
				logger.warn("Warnings insert Message: " + stmt.getWarnings());
				stmt.close();
				rs.close();
			}

			if (picID != null) {
				// wrong picture id
				if (!uploadedImages.containsKey(picID)) {
					throw new WebAppExcept(
							"Picture ID in field 'pic' not valid!", 400);
				}

				// create prepared statement for insert message into ry_messages
				stmt = con
						.prepareStatement("INSERT INTO ry_pictures (picture) VALUES (?)");
				stmt.setBytes(1, null);
				stmt.execute();

				rs = con.createStatement().executeQuery(
						"SELECT LAST_INSERT_ID() FROM ry_pictures");

				if (!rs.first()) {
					throw new SQLHandlerException(
							"Error while reading table entries.");
				}

				logger.trace("Picture ID: " + rs.getInt(1));

				picPos = rs.getInt(1);

				// add Thump Picture to BlobStore
				blobStore.put(this.getImageName(rs.getInt(1), 't'),
						PictureScaler.scaleSameAspect(
								uploadedImages.get(picID), thump_width,
								thump_height));
				// add Small Picture to BlobStore
				blobStore.put(this.getImageName(rs.getInt(1), 's'),
						PictureScaler.scaleSameAspect(
								uploadedImages.get(picID), small_width,
								small_height));
				// add Full Picture to BlobStore
				blobStore.put(this.getImageName(rs.getInt(1), 'l'),
						uploadedImages.get(picID));

				// if loaded once remove from array
				uploadedImages.remove(picID);

				// debug
				logger.warn("Warnings insert Picture: " + stmt.getWarnings());
				stmt.close();
				rs.close();
			}

			if ((message != null && !message.isEmpty()) || picID != null) {

				// create new chat entry into ry_chats
				stmt = con
						.prepareStatement("INSERT INTO ry_chats (timestamp,groupID,senderHash,messageID,pictureID,chatroomID) VALUES "
								+ "(now(),(SELECT groupID FROM ry_clients WHERE gcmRegID = ?),MD5(?),?,?,?)");

				stmt.setString(1, gcm);
				stmt.setString(2, gcm);
				if (message != null && !message.isEmpty()) {
					stmt.setInt(3, mesPos);
				} else {
					stmt.setNull(3, Types.INTEGER);
				}
				if (picID != null) {
					stmt.setInt(4, picPos);
				} else {
					stmt.setNull(4, Types.INTEGER);
				}
				stmt.setInt(5, chatroom);

				stmt.execute();
				// debug
				logger.warn("Warnings insert ChatEntry: " + stmt.getWarnings());
				stmt.close();

			} else {
				con.rollback(sPoint);
				con.releaseSavepoint(sPoint);
				if (picPos != -1) {
					blobStore.remove(this.getImageName(picPos, 't'));
					blobStore.remove(this.getImageName(picPos, 's'));
					blobStore.remove(this.getImageName(picPos, 'l'));
				}
				throw new SQLHandlerException(
						"Add entry to database failed!: Unknown Error!");
			}

			// apply changes in database
			con.commit();

			stmt.close();
			con.close();

			// send update message to devices with Google Push Service
			this.updateDevices(chatroom);

		} catch (WebAppExcept e) {
			logger.catching(e);
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();

		} catch (SQLException e) {
			if (picPos != -1) {
				blobStore.remove(this.getImageName(picPos, 't'));
				blobStore.remove(this.getImageName(picPos, 's'));
				blobStore.remove(this.getImageName(picPos, 'l'));
			}
			try {
				con.rollback(sPoint);
				con.releaseSavepoint(sPoint);
			} catch (SQLException ignore) {
				logger.catching(ignore);
			}
			logger.catching(e);
			r = Response.status(500).entity(e.getMessage()).build();
		} catch (IOException e) {
			if (picPos != -1) {
				blobStore.remove(this.getImageName(picPos, 't'));
				blobStore.remove(this.getImageName(picPos, 's'));
				blobStore.remove(this.getImageName(picPos, 'l'));
			}
			try {
				con.rollback(sPoint);
				con.releaseSavepoint(sPoint);
			} catch (SQLException ignore) {
				logger.catching(ignore);
			}
			logger.catching(e);
			r = Response.status(500).entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			r = Response.status(500).entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
					logger.catching(ignore);
				}
			}
		}
		return logger.exit(r);
	}

	/**
	 * this method return the chat entry for this client and the given chatroom
	 * 
	 * @param req
	 *            an JSONObject with the Parameters: <li>- 'gcm' as String,</li>
	 *            <li>- 'chatroom' as int,</li><li>- 'timestamp' as int</li>
	 * @return an JSONArray with the chat entries as JSONObject with the
	 *         Parameters: <li>- 'timestmap',</li><li>- 'groupID',</li><li>- 'chatID',</li><li>-
	 *         'self' as boolean, true if the message was send by client given
	 *         in gcm</li><li>- 'message' (can be null)</li><li>
	 *         - 'picture' (can be null)</li>
	 * @author Felix H�bner
	 */
	public Response getChatEntries(JSONObject req) {
		logger.entry();
		Connection con = null;
		JSONArray lst = new JSONArray();
		JSONObject o = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Response r = null;

		String gcm = null;
		int chatroom = -1;
		int timestamp = 0;

		try {
			// check if field gcm exists
			if (!req.isNull("gcm")) {
				gcm = req.getString("gcm");
			} else {
				throw new WebAppExcept("field 'gcm' not found or null!", 400);
			}

			// check if field chatroom exists
			if (!req.isNull("chatroom")) {
				chatroom = req.getInt("chatroom");
			} else {
				throw new WebAppExcept("field 'chatroom' not found or null!",
						400);
			}

			// check if field timestamp exists
			if (!req.isNull("timestamp")) {
				timestamp = req.getInt("timestamp");
			} else {
				if (!req.has("timestamp")) {
					throw new WebAppExcept("field 'timestamp' not found!", 400);
				}
				timestamp = 0;
			}

			con = this.getSqlCon();

			// maybe we should remove this check for security reasons
			// (bruteforce a gcm id)
			rs = con.createStatement().executeQuery(
					"SELECT count(gcmRegID) FROM ry_clients WHERE gcmRegID = '"
							+ gcm + "'");
			if (!(rs.first() && rs.getInt(1) > 0)) {
				throw logger
						.throwing(new WebAppExcept("gcm id not found!", 401));
			}
			rs.close();
	
			//Ray: Fast SQL using LEFT JOINS
			stmt = con.prepareStatement("SELECT chatID, UNIX_TIMESTAMP(chats.timestamp) AS timestamp, msg.message, chats.groupID, chats.pictureID, (MD5(?)=chats.senderHash) AS self "+
										"FROM ry_chats AS chats "+
										"LEFT JOIN ry_messages AS msg USING ( messageID ) "+
										"LEFT JOIN ry_chatrooms_groups AS cg USING ( chatroomID ) "+
										"LEFT JOIN ry_clients AS clients ON cg.groupID = clients.groupID "+
										"WHERE chats.chatroomID =? "+
										"AND chats.timestamp >=FROM_UNIXTIME(?) "+
										"AND clients.gcmRegID =? "+
										"ORDER BY chats.timestamp");


			stmt.setString(1, gcm);
			stmt.setInt(2, chatroom);
			stmt.setInt(3, timestamp);
			stmt.setString(4, gcm);
			rs = stmt.executeQuery();

			Boolean available = rs.first();
			while (available) {
				o = new JSONObject();
				o.put("chatID", rs.getInt("chatID"));
				o.put("timestamp", rs.getInt("timestamp"));
				o.put("groupID", rs.getInt("groupID"));
				String msg = rs.getString("message"); //Ray: messageID not needed
				if (msg != null && msg.length() > 0) {
					o.put("message", rs.getString("message"));
				} else {
					o.put("message", JSONObject.NULL);
				}
				if (rs.getInt("pictureID") != 0) {
					o.put("picture", rs.getInt("pictureID"));
				} else {
					o.put("picture", JSONObject.NULL);
				}
				if (rs.getBoolean("self")) {
					o.put("self", true);
				} else {
					o.put("self", false);
				}
				// add object to array
				lst.put(o);

				// DEBUG
				// System.out.println(o.toString());

				// jump to next return row
				available = rs.next();
			}
			rs.close();
			stmt.close();
			con.close();

			r = Response.ok().entity(lst).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a 400 user error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return logger.exit(r);

	}

	// ==================================================================//
	// Pictures Commands
	// ==================================================================//

	/**
	 * this method except a byte[] image and will return a internal pictureID to
	 * access the picture again with setNewChatEntry()
	 * 
	 * @param pic
	 *            byte[] picture
	 * @return a Number as Long to access the picture again with
	 *         setNewChatEntry()
	 * @author Felix H�bner
	 */
	public Response setPicture(byte[] pic) {
		Response r = null;
		JSONObject o = null;
		Long l = null;
		logger.entry();
		if (pic == null) {
			r = Response.status(400).entity("no octetStream transmitted")
					.build();
		}

		ByteArrayInputStream in = new ByteArrayInputStream(pic);
		try {
			if (ImageIO.read(in) == null) {
				throw new WebAppExcept("not a valid pic format", 400);
			}
			in.close();

			l = new Long(System.nanoTime());
			uploadedImages.put(l, pic);
			o = new JSONObject();
			o.put("pic", l);
			r = Response.ok().entity(o).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a 400 user error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
		} catch (IOException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		}

		return logger.exit(r);
	}

	/**
	 * this method will return the picture in the given size as byte[]
	 * (octet-stream)
	 * 
	 * @param picID
	 *            of the image to return
	 * @param size
	 *            't' for "thumbnail", 's' for "small", 'l' for "large"
	 * @return image as byte[]
	 * @author Felix H�bner
	 */
	public Response returnPic(int picID, char size) {
		Response r = null;
		logger.entry();
		byte[] blob = blobStore.get(this.getImageName(picID, size));
		r = Response.ok().entity(blob).type("image/jpeg").build();
		if (blob == null) {
			r = Response.status(400)
					.entity("picID not found or not available in this size")
					.build();
		}
		return logger.exit(r);
	}

	/**
	 * this method creates the name for the blob entries
	 * 
	 * @param picID
	 *            running number of the picture from database
	 * @param size
	 *            't','s','l' to add at the end
	 * @return a String with the created name
	 * @author Felix H�bner
	 */
	private String getImageName(int picID, char size) {
		return String.format("%08d_%c", picID, size);
	}

	// ==================================================================//
	// User Commands
	// ==================================================================//

	/**
	 * this method will try to add the user to a database for this, the method
	 * will compare the password from the user and the password for the group
	 * 
	 * @param req
	 *            JSONOject with the parameters: <li>- 'gcm' as String,</li><li>
	 *            - 'groupID' as int,</li><li>- 'password' as String</li>
	 * @author Felix H�bner
	 */
	public Response registerUser(JSONObject req) {
		logger.entry();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray lst = new JSONArray();
		Response r = null;

		String gcm = null;
		int groupID = -1;
		String password = null;

		try {
			// check if field gcm exists
			if (!req.isNull("gcm") && !req.getString("gcm").isEmpty()) {
				gcm = req.getString("gcm");
			} else {
				throw new WebAppExcept("field 'gcm' not found or null!", 400);
			}

			// check if field chatroom exists
			if (!req.isNull("groupID")) {
				groupID = req.getInt("groupID");
			} else {
				throw new WebAppExcept("field 'groupID' not found or null!",
						400);
			}

			// check if field timestamp exists
			if (!req.isNull("password")) {
				password = req.getString("password");
			} else {
				throw new WebAppExcept("field 'password' not found!", 400);
			}

			con = this.getSqlCon();

			// check if user already loged in
			stmt = con
					.prepareStatement("SELECT groupID FROM ry_clients WHERE gcmRegId = ?");
			stmt.setString(1, gcm);
			rs = stmt.executeQuery();

			if (rs.first()) {
				throw new WebAppExcept("client with gcm=" + gcm
						+ " already logged in to group: "
						+ rs.getInt("groupID"), 403);
			}
			rs.close();
			stmt.close();

			// check if group exists
			stmt = con
					.prepareStatement("SELECT count(groupID) FROM ry_groups WHERE groupID = ? AND password=MD5(?)");
			stmt.setInt(1, groupID);
			stmt.setString(2, password);
			rs = stmt.executeQuery();
			if (!rs.first()) {
				throw new SQLException();
			}
			if (rs.getInt(1) != 1) {
				throw new WebAppExcept("groupID or password wrong.", 403);
			}
			rs.close();
			stmt.close();

			// add user to database
			stmt = con
					.prepareStatement("INSERT INTO ry_clients (groupID,gcmRegID) VALUES (?,?)");
			stmt.setInt(1, groupID);
			stmt.setString(2, gcm);
			stmt.execute();
			stmt.close();

			// get all chatrooms and add to result
			stmt = con //Ray: Added JOIN and additional column 'name'
					.prepareStatement("SELECT chatroomID, name FROM `ry_chatrooms` LEFT JOIN `ry_chatrooms_groups` USING (chatroomID) WHERE groupID = ?");
			stmt.setInt(1, groupID);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject o = new JSONObject(); //Ray: Added name:"Chatroom:name"
				o.put("chatroom", rs.getInt(1));
				o.put("name", rs.getString(2));
				lst.put(o);
			}
			rs.close();
			stmt.close();

			logger.info("Client with gcmID:" + gcm + " successful logged in.");
			r = Response.ok().entity(lst).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a 400 user error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
			logger.info("Client with gcmID:" + gcm + " failed to log in.");
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with the server error
			r = Response.serverError().entity(e.getMessage()).build();
			logger.info("Client with gcmID:" + gcm + " failed to log in.");
		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with the server error
			r = Response.serverError().entity(e.getMessage()).build();
			logger.info("Client with gcmID:" + gcm + " failed to log in.");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return logger.exit(r);
	}

	/**
	 * this method unregister a user from the database
	 * 
	 * @param req
	 *            a JSONObject with gcm as String.
	 * @return HTTP 201 if successful removed from database, otherwise HTTP 400
	 *         and a hint
	 * @author Felix H�bner
	 */
	public Response unregisterUser(JSONObject req) {
		logger.entry();
		Response r = null;

		String gcm = null;

		try {
			// check if field gcm exists
			if (!req.isNull("gcm")) {
				gcm = req.getString("gcm");
			} else {
				throw new WebAppExcept("field 'gcm' not found or null!", 400);
			}

			if (this.removeUser(gcm)) {
				logger.info("Client with gcmID:" + gcm
						+ " successful logged out.");
				r = Response.ok().entity("logout successful!").build();
			} else {
				logger.info("Client with gcmID:" + gcm + " failed to log out.");
				r = Response.serverError().build();
			}
		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a 400 request error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		}

		return logger.exit(r);
	}

	/**
	 * updates the gcm of a user
	 * 
	 * @param oldGCM
	 *            the old outdated gcm id
	 * @param newGCM
	 *            the new gcm id given from google
	 * @return true if successfull, otherwise false
	 * @author Felix H�bner
	 */
	public boolean updateUser(String oldGCM, String newGCM) {
		Connection con = null;
		PreparedStatement stmt = null;

		if (!(oldGCM != null && !oldGCM.isEmpty())) {
			return false;
		}
		if (!(newGCM != null && !newGCM.isEmpty())) {
			return false;
		}

		try {
			con = this.getSqlCon();

			// try to remove
			stmt = con
					.prepareStatement("UPDATE ry_clients SET gcmRegID = ? WHERE gcmRegID = ?");
			stmt.setString(1, newGCM);
			stmt.setString(2, oldGCM);
			stmt.executeUpdate();
			stmt.close();
			con.close();

		} catch (SQLException e) {
			logger.catching(e);
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * remove a user from database by a given gcm id
	 * 
	 * @param gcm
	 *            the gcm id to remove from database
	 * @return true if the removal was successful, otherwise false
	 * @author Felix H�bner
	 */
	public boolean removeUser(String gcm) {
		Connection con = null;
		PreparedStatement stmt = null;

		if (!(gcm != null && !gcm.isEmpty())) {
			return false;
		}

		try {
			con = this.getSqlCon();

			// try to remove
			stmt = con
					.prepareStatement("DELETE FROM ry_clients WHERE gcmRegID = ?");
			stmt.setString(1, gcm);
			stmt.executeUpdate();
			stmt.close();
			con.close();

		} catch (SQLException e) {
			logger.catching(e);
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
			return false;
		}
		return true;
	}

	// ==================================================================//
	// Map Commands
	// ==================================================================//

	/**
	 * return all nodes stored in this game
	 * 
	 * @return a JSONArray with JSONObjects, each have the following elements:
	 *         <li>nodeID: internal number must be used for each playing step</li>
	 *         <li>name: name of the node</li><li>lat: latitude of the node
	 *         (become a long)</li><li>lon: longitude of the node (become a
	 *         long)</li><li>description: description of the node (can be null)</li>
	 * @author Felix H�bner
	 */
	public Response getAllNodes() {
		Connection con = null;
		java.sql.Statement stmt = null;
		JSONArray lst = new JSONArray();
		ResultSet rs = null;
		Response r = null;

		try {
			con = this.getSqlCon();

			stmt = con.createStatement();
			rs = stmt
					.executeQuery("SELECT NodeID,name,lat,lon,description FROM ry_nodes");

			while (rs.next()) {

				JSONObject a = new JSONObject();

				a.put("nodeID", rs.getInt("NodeID"));
				a.put("name", rs.getString("name"));
				a.put("lat", rs.getDouble("lat"));
				a.put("lon", rs.getDouble("lon"));
				if (rs.getString("description") != null) {
					a.put("description", rs.getString("description"));
				} else {
					a.put("description", JSONObject.NULL);
				}

				lst.put(a);
			}
			rs.close();
			stmt.close();
			con.close();

			r = Response.ok().entity(lst).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return r;
	}

	/**
	 * return all edges stored in this game
	 * 
	 * @return a JSONArray with JSONObjects, each have the following elements:
	 *         <li>nodeA: internalID of a node</li> <li>nodeB: internalID if a
	 *         node</li> <li>type: type of the connection (possible is:
	 *         'foot','bike','bus','tram'</li>
	 * @author Felix H�bner
	 */
	public Response getAllEdges() {
		Connection con = null;
		java.sql.Statement stmt = null;
		JSONArray lst = new JSONArray();
		ResultSet rs = null;
		Response r = null;

		try {
			con = this.getSqlCon();

			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT nodeA,nodeB,type FROM ry_edges");

			while (rs.next()) {

				JSONObject a = new JSONObject();

				a.put("nodeA", rs.getInt("nodeA"));
				a.put("nodeB", rs.getInt("nodeB"));
				a.put("type", rs.getString("type"));
				lst.put(a);
			}
			rs.close();
			stmt.close();
			con.close();

			r = Response.ok().entity(lst).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return r;
	}

	// ==================================================================//
	// System Commands
	// ==================================================================//

	/**
	 * this method returns the status of the current game as JSON Object to the
	 * client
	 * 
	 * @return a JSON Object with: <li>curRound: current round of the game</li>
	 *         <li>serverTime: current time on the server</li>
	 * @author Felix H�bner
	 * @param o
	 */
	public Response getStatus(JSONObject req) {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		JSONObject a = new JSONObject();
		ResultSet rs = null;
		Response r = null;
		String gcm = null;

		try {
			con = this.getSqlCon();

			// check if field gcm exists
			if (!req.isNull("gcm")) {
				gcm = req.getString("gcm");
			} else {
				throw new WebAppExcept("field 'gcm' not found or null!", 400);
			}

			// check if the user is logged in
			pstmt = con
					.prepareStatement("SELECT count(clientID) FROM ry_clients WHERE gcmRegID=?");
			pstmt.setString(1, gcm);
			rs = pstmt.executeQuery();
			if (!rs.first()) {
				// something is wrong in SQL-statement or sql-connection.
				throw new SQLException(
						"Failed to get count of users with gcmRegID");
			}
			if (rs.getInt(1) != 1) {
				// user not logged in
				throw new WebAppExcept("User Unauthorized", 403);
			}
			pstmt.close();
			rs.close();

			// get the status settings from server
			stmt = con.createStatement();
			rs = stmt
					.executeQuery("SELECT MAX(roundID) as round, UNIX_TIMESTAMP() as serverTime FROM `ry_rounds` LIMIT 1");

			while (rs.next()) {

				a.put("curRound", rs.getInt("round"));
				a.put("serverTime", rs.getString("serverTime"));

			}
			rs.close();
			stmt.close();
			con.close();

			r = Response.ok().entity(a).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a user error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return r;
	}

	/**
	 * this method returns the current config of the server/game
	 * 
	 * @return a JSON Object with: <li>gameType: type of the game (possible
	 *         types are: stadtRallye, scotlandYard</li> <li>gameName: name of
	 *         the gameserver</li><li>location_lat: place were the game will be
	 *         played - latitude</li><li>location_lon: place were the game will
	 *         be played - longtitude</li><li>an array of the different tickets
	 *         and count</li> <li>rounds: rounds to be played</li><li>roundTime:
	 *         time between 2 rounds in minutes</li><li>gameStartTime: time were
	 *         the first round starts</li><li>freeStartPoint: true if the group
	 *         can decide which is the start node, otherwise false</li>
	 * @author Felix H�bner
	 */
	public Response getConfig() {
		Connection con = null;
		PreparedStatement pstmt = null;
		JSONObject a = new JSONObject();
		ResultSet rs = null;
		Response r = null;

		try {
			con = this.getSqlCon();

			// check if the user is logged in
			pstmt = con
					.prepareStatement("SELECT gameType, gameName, location_lat, location_lon, tickets_bike, tickets_foot, tickets_tram, tickets_bus, rounds, roundTime, UNIX_TIMESTAMP( gameStartTime ) as gameStartTime, freeStartpoint FROM ry_config ORDER BY configID DESC LIMIT 1");
			rs = pstmt.executeQuery();
			if (!rs.first()) {
				// something is wrong in SQL-statement or sql-connection.
				throw new SQLException("Failed to get a valid config setting");
			}

			a.put("gameType", rs.getString("gameType"));
			a.put("gameName", rs.getString("gameName"));
			a.put("locLat", rs.getDouble("location_lat"));
			a.put("locLon", rs.getDouble("location_lon"));
			JSONArray ar = new JSONArray();
			ar.put((new JSONObject()).put("bike", rs.getInt("tickets_bike")));
			ar.put((new JSONObject()).put("foot", rs.getInt("tickets_foot")));
			ar.put((new JSONObject()).put("tram", rs.getInt("tickets_tram")));
			ar.put((new JSONObject()).put("bus", rs.getInt("tickets_bus")));
			a.put("tickets", ar);
			a.put("rounds", rs.getInt("rounds"));
			a.put("roundTime", rs.getInt("roundTime"));
			a.put("gameStartTime", rs.getInt("gameStartTime"));
			a.put("freeStartpoint", rs.getBoolean("freeStartpoint"));

			rs.close();
			pstmt.close();
			con.close();

			r = Response.ok().entity(a).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (WebAppExcept e) {
			logger.catching(e);
			// exchange the response with a user error
			r = Response.status(e.getResponse().getStatus())
					.entity(e.getMessage()).build();
		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			r = Response.serverError().entity(e.getMessage()).build();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return r;
	}

	/**
	 * read gameStartTime, rounds and roundtime from database and return them as
	 * JSONArray
	 * 
	 * @return JSONArray with roundtime, rounds and gameStartTime, if an error
	 *         occure <i>null</i> will be returned
	 */
	public JSONObject getControlData() {
		Connection con = null;
		PreparedStatement pstmt = null;
		JSONObject a = new JSONObject();
		ResultSet rs = null;

		try {
			con = this.getSqlCon();

			// check if the user is logged in
			pstmt = con
					.prepareStatement("SELECT rounds, roundTime, UNIX_TIMESTAMP( gameStartTime ) as gameStartTime, (SELECT COUNT(roundID) FROM ry_rounds) as currentRound FROM ry_config ORDER BY configID DESC LIMIT 1");
			rs = pstmt.executeQuery();
			if (!rs.first()) {
				// something is wrong in SQL-statement or sql-connection.
				throw new SQLException("Failed to get a valid config setting");
			}

			a.put("rounds", rs.getInt("rounds"));
			a.put("roundTime", rs.getInt("roundTime"));
			a.put("gameStartTime", rs.getInt("gameStartTime"));
			a.put("currentRound", rs.getInt("currentRound"));

			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			logger.catching(e);
			// exchange the response with a server error
			a = null;
		} catch (JSONException e) {
			logger.catching(e);
			// exchange the response with a server error
			a = null;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignore) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
		return a;
	}

	/**
	 * collect all clients from database and send a game started command to the
	 * clients
	 * 
	 * @param time
	 */
	public void startGame(long time) {
		
		logger.entry();
		HashMap<String, Integer> lst = new HashMap<String, Integer>();
		try {
			Connection sql = this.getSqlCon();
			
			//add entry to rounds table
			PreparedStatement pstmt = sql.prepareStatement("INSERT INTO ry_rounds (start, preparation) VALUES (FROM_UNIXTIME(?), 1)");
			pstmt.setLong(1, time);
			pstmt.executeUpdate();
			
			// select all clients to send them the info that the game is started
			ResultSet rs = sql.createStatement()
					.executeQuery("SELECT gcmRegID,apnRegID FROM ry_clients");
			Boolean empty = rs.first();

			while (empty) {
				if (rs.getString(1) != null) {
					lst.put(rs.getString(1), PushService.DEVICE_GOOGLE);
					// DEBUG
					logger.trace("Update Client with id: " + rs.getString(1));
				} else {
					lst.put(rs.getString(2), PushService.DEVICE_APPLE);
					// DEBUG
					logger.trace("Update Client with id: " + rs.getString(1));
				}
				// jump to next client
				empty = rs.next();
			}
			// push the info to the clients
			pushService.startGame(lst, time);

		} catch (SQLException e) {
			logger.catching(e);
		} catch (PushServiceException e) {
			logger.catching(e);
		}
		logger.info("Send to " + lst.size() + " Devices:  Game Started!");

		logger.exit();
	}

	public void startNextRound(long timestamp) {
		logger.entry();
		PreparedStatement pstmt = null;
		Savepoint sPoint = null;
		Connection sql = null;

		HashMap<String, Integer> lst = new HashMap<String, Integer>();
		try {
			sql = this.getSqlCon();
			sql.setAutoCommit(false);
			sPoint = sql.setSavepoint();
			// set current round to complete

			sql.createStatement()
					.executeUpdate(
							"UPDATE ry_rounds SET preparation = 0 WHERE preparation = 1");

			pstmt = sql
					.prepareStatement("INSERT INTO ry_rounds (start ,preparation) VALUES (FROM_UNIXTIME(?), 1)");
			pstmt.setLong(1, timestamp);
			pstmt.executeUpdate();

			// do changes in the database
			sql.commit();
			sPoint = null;
			pstmt.close();

			// collect all clients to send them the info about a new round
			ResultSet rs = sql.createStatement().executeQuery(
					"SELECT gcmRegID,apnRegID FROM ry_clients");
			Boolean empty = rs.first();

			while (empty) {
				if (rs.getString(1) != null) {
					lst.put(rs.getString(1), PushService.DEVICE_GOOGLE);
					// DEBUG
					logger.trace("Update Client with id: " + rs.getString(1));
				} else {
					lst.put(rs.getString(2), PushService.DEVICE_APPLE);
					// DEBUG
					logger.trace("Update Client with id: " + rs.getString(1));
				}
				// jump to next client
				empty = rs.next();
			}
			rs.close();

			// push the info to the clients
			pushService.nextRound(lst, timestamp);

		} catch (SQLException e) {
			logger.catching(e);
		} catch (PushServiceException e) {
			logger.catching(e);
		} finally {
			if (sPoint != null && sql != null) {
				try {
					sql.rollback(sPoint);
					sPoint = null;
				} catch (SQLException e) {
					logger.catching(e);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					logger.catching(e);
				}
			}
			if (sql != null) {
				try {
					sql.close();
				} catch (SQLException e) {
					logger.catching(e);
				}
			}
		}
		logger.info("Send to " + lst.size() + " devices:  Next Round Started!");

		logger.exit();
	}
}
