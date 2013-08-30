package de.rallye.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.rallye.auth.GroupPrincipal;
import de.rallye.auth.RallyePrincipal;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.model.structures.AdditionalResource;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.GroupUser;
import de.rallye.model.structures.LatLng;
import de.rallye.model.structures.LoginInfo;
import de.rallye.model.structures.Node;
import de.rallye.model.structures.PushConfig;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.ServerConfig;
import de.rallye.model.structures.SimpleChatEntry;
import de.rallye.model.structures.SimpleSubmission;
import de.rallye.model.structures.Submission;
import de.rallye.model.structures.Task;
import de.rallye.model.structures.TaskSubmissions;
import de.rallye.model.structures.UserAuth;
import de.rallye.model.structures.UserInternal;

public class DataAdapter {
	
	private static Logger logger = LogManager.getLogger(DataAdapter.class.getName());
	
	private ComboPooledDataSource dataSource;

	
	public DataAdapter(ComboPooledDataSource dataSource) throws SQLException {
		this.dataSource = dataSource;
		
		loadNodes();
		loadEdges();
	}
	
	/**
	 * appends a list of string separated by ", "
	 * @param strings
	 * @return
	 */
	private static String cols(String... strings) {
		StringBuilder b = new StringBuilder();
		
		int l = strings.length-1;
		for (int i=0; i<=l; i++) {
			b.append(strings[i]);
			if (i < l)
				b.append(", ");
		}
		
		return b.toString();
	}
	
	public void closeConnection() {
		dataSource.close();
	}
	
	private static void close(Connection con, Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	
	private List<Group> groups;
	
	public synchronized List<Group> getGroups() throws DataException {

		if (groups!=null) return groups;
		
		Statement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ cols(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION) +" FROM "+ Ry.Groups.TABLE);

			groups = new ArrayList<Group>();
			
			while (rs.next()) {
				groups.add(new Group(rs.getInt(1), rs.getString(2), rs.getString(3)));
			}
			
			return groups;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public List<Task> getTasks() throws DataException {
		Statement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ cols(Ry.Tasks.ID, Ry.Tasks.NAME, Ry.Tasks.DESCRIPTION,
					Ry.Tasks.LATITUDE, Ry.Tasks.LONGITUDE, Ry.Tasks.LOCATION_SPECIFIC,
					Ry.Tasks.RADIUS, Ry.Tasks.MULTIPLE_SUBMITS, Ry.Tasks.SUBMIT_TYPE, Ry.Tasks.POINTS, Ry.Tasks.ADDITIONAL_RESOURCES) +" FROM "+ Ry.Tasks.TABLE);

			List<Task> tasks = new ArrayList<Task>();
			
			while (rs.next()) {
				
				LatLng coords = (rs.getDouble(4) != 0 || rs.getDouble(5) != 0)? new LatLng(rs.getDouble(4), rs.getDouble(5)) : null;
				tasks.add(new Task(rs.getInt(1), rs.getBoolean(6), coords, rs.getDouble(7),
						rs.getString(2), rs.getString(3), rs.getBoolean(8), rs.getInt(9),
						rs.getString(10), AdditionalResource.additionalResourcesFromString(rs.getString(11))));
			}
			
			return tasks;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public List<Submission> getSubmissions(int taskID, int groupID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SCORE, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION) +" FROM "+ Ry.Submissions.TABLE
					+" WHERE "+ Ry.Submissions.ID_TASK +"=? AND "+ Ry.Submissions.ID_GROUP +"=?");
			
			st.setInt(1, taskID);
			st.setInt(2, groupID);
			
			rs = st.executeQuery();

			List<Submission> submissions = new ArrayList<Submission>();
			
			while (rs.next()) {
				submissions.add(new Submission(rs.getInt(1), rs.getInt(6), (Integer)rs.getObject(7), rs.getString(8), rs.getString(5)));
			}
			
			return submissions;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public List<TaskSubmissions> getAllSubmissions(int groupID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SCORE, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION) +" FROM "+ Ry.Submissions.TABLE
					+" WHERE "+ Ry.Submissions.ID_GROUP +"=? ORDER BY "+ Ry.Submissions.ID_TASK +" ASC");
			
			st.setInt(1, groupID);
			
			rs = st.executeQuery();

			List<TaskSubmissions> taskSubmissions = new ArrayList<TaskSubmissions>();
			List<Submission> submissions = null;
			
			int taskID = -1, lastID = -1;
			while (rs.next()) {
				lastID = taskID;
				taskID = rs.getInt(2);
				
				if (taskID != lastID) {
					submissions = new ArrayList<Submission>();
					taskSubmissions.add(new TaskSubmissions(taskID, submissions));
				}
				
				submissions.add(new Submission(rs.getInt(1), rs.getInt(6), (Integer)rs.getObject(7), rs.getString(8), rs.getString(5)));
			}
			
			return taskSubmissions;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public Submission submit(int taskID, int groupID, int userID, SimpleSubmission submission) throws DataException, InputException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			
			st = con.prepareStatement("SELECT * FROM "+ Ry.Tasks.TABLE +" WHERE "+ Ry.Tasks.ID +"=?");
			st.setInt(1, taskID);
			rs = st.executeQuery();
			if (!rs.first())
				throw new InputException("taskID does not exist");
			st.close();
			rs.close();
			
			//TODO: sanity check: see if the submission matches submitType and as such is valid
			

			st = con.prepareStatement("INSERT INTO "+ Ry.Submissions.TABLE +" ("+
					cols(Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP, Ry.Submissions.ID_USER, Ry.Submissions.SUBMIT_TYPE, Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION)
					+") VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			
			st.setInt(1, taskID);
			st.setInt(2, groupID);
			st.setInt(3, userID);
			st.setInt(4, submission.submitType);
			if (submission.intSubmission != null) {
				st.setInt(5, submission.intSubmission);
			} else {
				st.setNull(5, java.sql.Types.INTEGER);
			}
			st.setString(6, submission.textSubmission);
			
			st.execute();
			
			rs = st.getGeneratedKeys();
			rs.next();
			int submissionID = rs.getInt(1);
			
			Submission res = new Submission(submissionID, submission);
			
			return res;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	private Map<Integer,Node> nodes;
	private List<Edge> edges;
	
	public Map<Integer,Node> getNodes() {
		return nodes;
	}
	
	private void loadNodes() throws SQLException {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ cols(Ry.Nodes.ID, Ry.Nodes.NAME, Ry.Nodes.LAT, Ry.Nodes.LON, Ry.Nodes.DESCRIPTION) +" FROM "+ Ry.Nodes.TABLE +" ORDER BY "+ Ry.Nodes.ID);

			nodes = new HashMap<Integer,Node>();
			
			while (rs.next()) {
				nodes.put(rs.getInt(1),new Node(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getString(5)));
			}

		} finally {
			close(con, st, rs);
		}
	}

	public List<Edge> getEdges() {
		return edges;
	}
	
	private void loadEdges() throws SQLException{
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ cols(Ry.Edges.A, Ry.Edges.B, Ry.Edges.TYPE) +" FROM "+ Ry.Edges.TABLE);

			edges = new ArrayList<Edge>();
			
			while (rs.next()) {
				Node a = nodes.get(rs.getInt(1));
				Node b = nodes.get(rs.getInt(2));
				edges.add(new Edge(a, b, rs.getString(3)));
			}

		} finally {
			close(con, st, rs);
		}
	}
	
	public ServerConfig getServerConfig() throws DataException {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+
					cols(Ry.Config.NAME, Ry.Config.LAT, Ry.Config.LON,
							Ry.Config.ROUNDS, Ry.Config.ROUND_TIME, "UNIX_TIMESTAMP("+ Ry.Config.START_TIME +")") +" FROM "+ Ry.Config.TABLE);
			
			rs.next();
			
			ServerConfig res = new ServerConfig(rs.getString(1), rs.getDouble(2), rs.getDouble(3),
					rs.getInt(4), rs.getInt(5), rs.getLong(6));
			
			return res;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public RallyePrincipal getKnownUserAuthorization(int groupID, int userID, String password) throws DataException, UnauthorizedException, InputException {
		if (groupID <= 0 || userID <= 0 || password == null || password.length() <= 0)
			throw new InputException("Incomplete Login");
		
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {

			
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.ID_GROUP, "usr."+ Ry.Users.NAME, Ry.Groups_Chatrooms.ID_CHATROOM)
												+" FROM "+ Ry.Users.TABLE +" AS usr LEFT JOIN "+ Ry.Groups.TABLE +" as grp USING("+ Ry.Users.ID_GROUP +")"
												+" LEFT JOIN "+ Ry.Groups_Chatrooms.TABLE +" USING ("+ Ry.Groups_Chatrooms.ID_GROUP +")"
												+" WHERE "+ Ry.Users.ID +"=? AND "+ Ry.Groups.ID +"=? AND usr."+ Ry.Users.PASSWORD +"=?");
			st.setInt(1, userID);
			st.setInt(2, groupID);
			st.setString(3, password);
			
			rs = st.executeQuery();
			
			if (rs.first()) {
				String name = rs.getString(3);
				List<String> rights = new ArrayList<String>();
				do {
					rights.add("chatroom:"+ rs.getInt(4));
				} while (rs.next());
				
				return new RallyePrincipal(userID, groupID, name, rights);
			} else {
				throw new UnauthorizedException();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}		
	}
	
	public GroupPrincipal getNewUserAuthorization(int groupID, String password) throws DataException, UnauthorizedException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Groups.ID, Ry.Groups.NAME)
												+" FROM "+ Ry.Groups.TABLE
												+" WHERE "+ Ry.Groups.ID +"=? AND "+ Ry.Groups.PASSWORD +"=?");
			st.setInt(1, groupID);
			st.setString(2, password);
			
			rs = st.executeQuery();
			
			if (rs.first()) {
				return new GroupPrincipal(groupID, rs.getString(2));
			} else {
				throw new UnauthorizedException();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	@SuppressWarnings("resource")
	public UserAuth login(int groupID, LoginInfo info) throws DataException, InputException {
		invalidateUsers();
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			
			con = dataSource.getConnection();
			
			String password = generateNewUserPassword(groupID);
			
			int pushModeID = 0;
			
			if (info.pushMode != null) {
				st = con.prepareStatement("SELECT "+ Ry.PushModes.ID +" FROM "+ Ry.PushModes.TABLE +" WHERE "+ Ry.PushModes.NAME +" LIKE ? OR "+ Ry.PushModes.ID +"=?");
				st.setString(1, info.pushMode);
				st.setString(2, info.pushMode);
				
				rs = st.executeQuery();
				
				if (!rs.first())
					throw new InputException("PushMode '"+ info.pushMode +"' not supported");
				
				pushModeID = rs.getInt(1);
				
				close(null, st, rs);
			}
			
			int userID = -1;
			
			if (info.uniqueID != null && info.uniqueID.length() > 3) {
				st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.ID_GROUP, Ry.Users.ID_PUSH_MODE, Ry.Users.NAME, Ry.Users.PASSWORD, Ry.Users.PUSH_ID, Ry.Users.UNIQUE_ID) +
											" FROM "+ Ry.Users.TABLE +" WHERE "+ Ry.Users.UNIQUE_ID +" LIKE ?");
				st.setString(1, info.uniqueID);
				rs = st.executeQuery();
				
				if (rs.first()) {
					userID = rs.getInt(1);
					password = rs.getString(5);
				}
				close(null, st, rs);
			}
			
			if (userID > 0) {
				st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+
						Ry.Users.ID_GROUP +"=?, "+ Ry.Users.ID_PUSH_MODE +"=?, "+ Ry.Users.NAME +"=?, "+ Ry.Users.PASSWORD +"=?, "+ Ry.Users.PUSH_ID +"=? "+
						"WHERE "+ Ry.Users.ID +"=?");
				st.setInt(1, groupID);
				st.setInt(2, pushModeID);
				st.setString(3, info.name);
				st.setString(4, password);
				st.setString(5, info.pushID);
				st.setInt(6, userID);
				
				if (st.executeUpdate() > 0) {
					return new UserAuth(userID, password);
				} else {
					throw new DataException("User could not be reused");
				}
			} else {
				st = con.prepareStatement("INSERT INTO "+ Ry.Users.TABLE +" ("+ cols(Ry.Users.ID_GROUP, Ry.Users.PASSWORD, Ry.Users.NAME, Ry.Users.UNIQUE_ID, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +")" +
						" VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
				
				st.setInt(1, groupID);
				st.setString(2, password);
				st.setString(3, info.name);
				st.setString(4, info.uniqueID);
				st.setInt(5, pushModeID);
				st.setString(6, info.pushID);
				st.execute();
				
				rs = st.getGeneratedKeys();
				
				if (rs.first()) {
					return new UserAuth(rs.getInt(1), password);
				} else {
					throw new DataException("User could not be created");
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	Random rnd = new Random();
	private String generateNewUserPassword(int groupID) {
		long pw = System.currentTimeMillis() + groupID + rnd.nextInt();
		
		return Long.toHexString(pw);
	}
	
	public boolean logout(int groupID, int userID) throws DataException {
		invalidateUsers();
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
//			st = con.prepareStatement("DELETE FROM "+ Ry.Users.TABLE +
//					" WHERE "+ Ry.Users.ID +"=?");
			
			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.ID_PUSH_MODE +"=? "+//TODO: set status=offline?
										"WHERE "+ Ry.Users.ID +"=?");
			
			st.setInt(1, 0);
			st.setInt(2, userID);
			st.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public boolean hasRightsForChatroom(int groupID, int roomID) throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT *"
					+" FROM "+ Ry.Groups_Chatrooms.TABLE
					+" WHERE "+ Ry.Groups_Chatrooms.ID_GROUP +"=? AND "+ Ry.Groups_Chatrooms.ID_CHATROOM +"=?");
			st.setInt(1, groupID);
			st.setInt(2, roomID);
			rs = st.executeQuery();
			
			rs = st.executeQuery();
			
			return rs.first();
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public synchronized List<Chatroom> getChatrooms(int groupID) throws DataException { // Somebody (!!!Jakob!!!) implemented caching of available Chatrooms!!!
		Connection con = null;// And he was smart enough to do it in 1 SINGLE list for ALL groups
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Chatrooms.ID, Ry.Chatrooms.NAME)
					+" FROM "+ Ry.Groups_Chatrooms.TABLE +" LEFT JOIN "+ Ry.Chatrooms.TABLE +" USING ("+ Ry.Chatrooms.ID +")"
					+"WHERE "+ Ry.Groups_Chatrooms.ID_GROUP +"=?");
			st.setInt(1, groupID);
			rs = st.executeQuery();
			
			List<Chatroom> chatrooms = new ArrayList<Chatroom>();
			
			while (rs.next()) {
				chatrooms.add(new Chatroom(rs.getInt(1), rs.getString(2)));
			}
			
			return chatrooms;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public List<ChatEntry> getChats(int roomID, long timestamp, int groupID) throws DataException, UnauthorizedException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
	
			//TODO: throw Unauthorized if no rights to access chatroom
			st = con.prepareStatement("SELECT "+ cols(Ry.Chats.ID, Ry.Messages.MSG, "UNIX_TIMESTAMP("+Ry.Chats.TIMESTAMP+")", Ry.Chats.ID_USER, "chats."+Ry.Chats.ID_GROUP, Ry.Chats.ID_PICTURE)+
										" FROM "+ Ry.Chats.TABLE +" AS chats"+
										" LEFT JOIN "+ Ry.Messages.TABLE +" AS msg USING ("+ Ry.Chats.ID_MESSAGE +") "+
										"LEFT JOIN "+ Ry.Groups_Chatrooms.TABLE +" AS gc USING ("+ Ry.Chats.ID_CHATROOM +") "+
										"WHERE chats."+Ry.Chats.ID_CHATROOM +" =? "+
										"AND gc."+Ry.Groups_Chatrooms.ID_GROUP +" =? "+
										"AND "+ Ry.Chats.TIMESTAMP +" >=FROM_UNIXTIME(?) "+
										"ORDER BY "+ Ry.Chats.TIMESTAMP);


			st.setInt(1, roomID);
			st.setInt(2, groupID);
			st.setLong(3, timestamp);
			rs = st.executeQuery();

			List<ChatEntry> chats = new ArrayList<ChatEntry>();
			
			while (rs.next()) {
				chats.add(new ChatEntry(rs.getInt(1), rs.getString(2), rs.getLong(3), rs.getInt(5), rs.getInt(4), rs.getInt(6)));
			}

			if (chats.size() == 0) {
				rs.close();
				st.close();
				st = con.prepareStatement("SELECT count(*) FROM "+ Ry.Groups_Chatrooms.TABLE +
						" WHERE "+ Ry.Groups_Chatrooms.ID_CHATROOM +" =? "+
						"AND "+ Ry.Groups_Chatrooms.ID_GROUP +" =?");
				st.setInt(1, roomID);
				st.setInt(2, groupID);
				
				rs = st.executeQuery();
				
				if (!rs.first())
					throw new UnauthorizedException();
			}
			
			return chats;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public ChatEntry addChat(SimpleChatEntry chat, int roomID, int groupID, int userID) throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Savepoint transaction = null;
		boolean success = false;
		
		try {
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			
			transaction = con.setSavepoint();

			//Insert message
			st = con.prepareStatement("INSERT INTO "+ Ry.Messages.TABLE +" ("+ Ry.Messages.MSG +") VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			st.setString(1, chat.message);
			st.execute();

			rs = st.getGeneratedKeys();
			rs.first();

			int msgID = rs.getInt(1);

			rs.close();
			st.close();
			
			//TODO: check for existance of picture
			//Insert Chat
			st = con.prepareStatement("INSERT INTO "+ Ry.Chats.TABLE +" ("+
					cols(Ry.Chats.ID_CHATROOM, Ry.Chats.ID_USER, Ry.Chats.ID_GROUP, Ry.Chats.ID_MESSAGE, Ry.Chats.ID_PICTURE)
					+") VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			
			st.setInt(1, roomID);
			st.setInt(2, userID);
			st.setInt(3, groupID);
			st.setInt(4, msgID);
			if (chat.hasPicture()) {
				st.setInt(5, chat.pictureID);
			} else {
				st.setNull(5, java.sql.Types.INTEGER);
			}
			
			st.execute();
			
			rs = st.getGeneratedKeys();
			rs.next();
			int chatID = rs.getInt(1);
			
			rs.close();
			st.close();
			
			st = con.prepareStatement("SELECT UNIX_TIMESTAMP("+ Ry.Chats.TIMESTAMP +") FROM "+ Ry.Chats.TABLE +" WHERE "+ Ry.Chats.ID +"=?");
			st.setInt(1, chatID);
			rs = st.executeQuery();
			rs.first();
			long timestamp = rs.getLong(1);
			
			ChatEntry newChat = new ChatEntry(chatID, chat.message, timestamp, groupID, userID, chat.pictureID);
			
			success = true;
			//TODO: notifiyNewChatEntry()
			
			return newChat;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			try {
				if (success) {
					con.commit();
				} else {
					con.rollback(transaction);
					logger.error("Rolling back new chat");
				}
			} catch (Exception e) {}
			
			close(con, st, rs);
		}
	}

	public void setPushConfig(int groupID, int userID, PushConfig push) throws DataException {
		
		invalidateUsers();
		
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.PUSH_ID +"=?, " + Ry.Users.ID_PUSH_MODE +
										"=(SELECT "+ Ry.PushModes.ID +" FROM "+ Ry.PushModes.TABLE +" WHERE "+ Ry.PushModes.NAME +" LIKE ?)"+
										" WHERE "+ Ry.Users.ID +"=?");
			
			st.setString(1, push.pushID);
			st.setString(2, push.pushMode);
			st.setInt(3, userID);
			int count = st.executeUpdate();
			
			if (count > 1) {
				throw new DataException("Impossible!!!!");
			} else if (count < 1) {
				throw new DataException("Push Configuration change failed");
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	public PushConfig getPushConfig(int groupID, int userID) throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols("push."+Ry.PushModes.NAME, "usr."+Ry.Users.PUSH_ID) +
										" FROM "+ Ry.Users.TABLE +" AS usr LEFT JOIN "+ Ry.PushModes.TABLE +" AS push USING("+ Ry.Users.ID_PUSH_MODE +")"+
										" WHERE "+ Ry.Users.ID +"=? AND "+ Ry.Users.ID_GROUP +"=?");
			
			st.setInt(1, userID);
			st.setInt(2, groupID);
			rs = st.executeQuery();
			
			if (!rs.first()) {
				throw new DataException("User is not in group (should never have gotten here)");
			}
			
			PushConfig cfg = new PushConfig(rs.getString(2), rs.getString(1));
			return cfg;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	List<PushMode> pushModes;
	public synchronized List<PushMode> getPushModes() throws DataException {
		if (pushModes!=null) return pushModes;
		
		Statement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery("SELECT "+ cols(Ry.PushModes.ID, Ry.PushModes.NAME) +" FROM "+ Ry.PushModes.TABLE);

			pushModes = new ArrayList<PushMode>();
			
			while (rs.next()) {
				pushModes.add(new PushMode(rs.getInt(1), rs.getString(2)));
			}

			return pushModes;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	List<GroupUser> allUsers;
	public synchronized List<GroupUser> getAllUsers() throws DataException {
		if (allUsers!=null) return allUsers;
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.ID_GROUP, Ry.Users.NAME) +" FROM "+ Ry.Users.TABLE);
			
			rs = st.executeQuery();

			allUsers = new ArrayList<GroupUser>();
			
			while (rs.next()) {
				allUsers.add(new GroupUser(rs.getInt(1), rs.getInt(2), rs.getString(3)));
			}

			return allUsers;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	Map<Integer,List<UserInternal>> members = new HashMap<Integer,List<UserInternal>>();
	public synchronized List<UserInternal> getMembers(int groupID) throws DataException {
		
		List<UserInternal> cached = members.get(groupID);
		if (cached!=null) return cached;
		
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.NAME, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +" FROM "+ Ry.Users.TABLE +" WHERE "+ Ry.Users.ID_GROUP +"=?");
			st.setInt(1, groupID);
			
			rs = st.executeQuery();

			List<UserInternal> users = new ArrayList<UserInternal>();
			
			while (rs.next()) {
				users.add(new UserInternal(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
			}

			members.put(groupID, users);
			return users;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	protected void invalidateUsers() {
		logger.debug("Invalidating User Tables");
		allUsers = null;
		members.clear();
		roomMembers.clear();
	}
	

	Map<Integer,List<UserInternal>> roomMembers = new HashMap<Integer,List<UserInternal>>();
	
	public List<UserInternal> getChatroomMembers(int roomID) throws DataException {
		List<UserInternal> cached = roomMembers.get(roomID);
		if (cached!=null) return cached;
		
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Users.ID, Ry.Users.NAME, Ry.Users.ID_PUSH_MODE, Ry.Users.PUSH_ID) +
									" FROM "+ Ry.Users.TABLE +
									" LEFT JOIN "+ Ry.Groups_Chatrooms.TABLE +" USING("+ Ry.Groups_Chatrooms.ID_GROUP +")"+
									" WHERE "+ Ry.Groups_Chatrooms.ID_CHATROOM +"=?");
			st.setInt(1, roomID);
			
			rs = st.executeQuery();

			List<UserInternal> users = new ArrayList<UserInternal>();
			
			while (rs.next()) {
				users.add(new UserInternal(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
			}

			roomMembers.put(roomID,users);
			return users;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public int assignNewPictureID(int userID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("INSERT INTO "+ Ry.Pictures.TABLE +" ("+ cols(Ry.Pictures.ID_USER) +") VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			st.setInt(1, userID);
			
			st.execute();
			
			rs = st.getGeneratedKeys();
			
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new DataException("No new ID assigned by DB");
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public void editChatAddPicture(int chatID, int pictureID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("UPDATE "+ Ry.Chats.TABLE +" SET "+ Ry.Chats.ID_PICTURE +"=? WHERE "+ Ry.Chats.ID +"=?");
			st.setInt(1, pictureID);
			st.setInt(2, chatID);
			
			if (st.executeUpdate() > 0)
				return;
			else
				throw new DataException("Failed to add picture to Chat");
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	public void updatePushIds(HashMap<String, String> changes) throws DataException {
		invalidateUsers();
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("UPDATE "+ Ry.Users.TABLE +" SET "+ Ry.Users.PUSH_ID +"=? WHERE "+ Ry.Users.PUSH_ID +"=?");
			
			for (Entry<String, String> pair: changes.entrySet()) {
				st.setString(1, pair.getValue());
				st.setString(2, pair.getKey());
				
				st.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
}
