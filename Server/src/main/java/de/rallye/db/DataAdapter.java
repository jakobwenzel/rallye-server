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
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.rallye.config.RallyeConfig;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.filter.auth.AdminPrincipal;
import de.rallye.filter.auth.GroupPrincipal;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.model.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

public class DataAdapter implements IDataAdapter {
	
	private static final Logger logger = LogManager.getLogger(DataAdapter.class.getName());
	
	private final ComboPooledDataSource dataSource;
	private String dbName;

	
	public DataAdapter(ComboPooledDataSource dataSource) throws SQLException {
		this.dataSource = dataSource;

		Connection connection = dataSource.getConnection();
		connection.close();

		loadNodes();
		loadEdges();
	}

	public static DataAdapter getInstance(RallyeConfig config) {
		// create dataBase Handler
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			logger.error("MySQL Driver not found", e);
			return null;
		}
		RallyeConfig.DbConnectionConfig dbc = config.getDbConnectionConfig();

		dataSource.setJdbcUrl(dbc.connectString);
		dataSource.setUser(dbc.username);
		dataSource.setPassword(dbc.password);
		dataSource.setMaxIdleTime(dbc.maxIdleTime); // set max idle time to 1 hour

		DataAdapter da = null;
		try {
			da = new DataAdapter(dataSource);
			da.dbName = dbc.connectString.replaceFirst("^[^/]+//[^/]+/([^?]+).*$", "$1");
		} catch (SQLException e) {
			logger.error("Could not establish DB connection", e);
		}

		return da;
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

	public long getLastModified(String table) {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT UNIX_TIMESTAMP(UPDATE_TIME) FROM information_schema.tables WHERE TABLE_SCHEMA=? AND TABLE_NAME=?");

			st.setString(1, dbName);
			st.setString(2, table);

			rs = st.executeQuery();

			long timestamp;
			rs.first();
			timestamp = rs.getLong(1);

			return timestamp;
		} catch (SQLException e) {
			return 0;
		} finally {
			close(con, st, rs);
		}
	}

	@Override
	public long getNodesLastModified() {
		return getLastModified(Ry.Nodes.TABLE);
	}

	@Override
	public long getEdgesLastModified() {
		return getLastModified(Ry.Edges.TABLE);
	}

	@Override
	public long getTasksLastModified() {
		return getLastModified(Ry.Tasks.TABLE);
	}
	
	private List<Group> groups;
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getGroups()
	 */
	@Override
	public synchronized List<Group> getGroups(boolean includePasswords) throws DataException {

		if (groups!=null) return groups;
		
		Statement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.createStatement();

			if(includePasswords)
				rs = st.executeQuery("SELECT "+ cols(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION, Ry.Groups.PASSWORD) +" FROM "+ Ry.Groups.TABLE+" ORDER BY "+Ry.Groups.NAME);
			else
				rs = st.executeQuery("SELECT "+ cols(Ry.Groups.ID, Ry.Groups.NAME, Ry.Groups.DESCRIPTION) +" FROM "+ Ry.Groups.TABLE+" ORDER BY "+Ry.Groups.NAME);

			groups = new ArrayList<Group>();

			if (includePasswords)
				while (rs.next()) {
					groups.add(new Group(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
				}
			else
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
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getTasks()
	 */
	@Override
	public List<Task> getTasks(Integer groupID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		//Set to an invalid id if null.
		if (groupID==null) {
			logger.info("gid is null");
			groupID=0;
		}
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Tasks.TABLE+"."+Ry.Tasks.ID, Ry.Tasks.NAME, Ry.Tasks.DESCRIPTION,
					Ry.Tasks.LATITUDE, Ry.Tasks.LONGITUDE, Ry.Tasks.LOCATION_SPECIFIC,
					Ry.Tasks.RADIUS, Ry.Tasks.MULTIPLE_SUBMITS, Ry.Tasks.SUBMIT_TYPE, Ry.Tasks.POINTS,
					Ry.Tasks.ADDITIONAL_RESOURCES, Ry.Tasks_Groups.SCORE, Ry.Tasks_Groups.BONUS) +" FROM "+
					Ry.Tasks.TABLE+" LEFT JOIN "+Ry.Tasks_Groups.TABLE+" ON "+Ry.Tasks.TABLE+"."+Ry.Tasks.ID+"="+Ry.Tasks_Groups.TABLE+"."+Ry.Tasks_Groups.ID_TASK+" AND "+Ry.Groups.ID+"=?");
			
			st.setInt(1,groupID);
			logger.info("GID: "+groupID);
			rs = st.executeQuery();

			List<Task> tasks = new ArrayList<Task>();
			
			while (rs.next()) {
				
				LatLng coords = (rs.getDouble(4) != 0 || rs.getDouble(5) != 0)? new LatLng(rs.getDouble(4), rs.getDouble(5)) : null;
				Integer score = rs.getInt(12);
				if (rs.wasNull()) score = null; //Great API design guys!
				Integer bonus = rs.getInt(13);
				if (rs.wasNull()) bonus = null; //Great API design guys!
				tasks.add(new Task(rs.getInt(1), rs.getBoolean(6), coords, rs.getDouble(7),
						rs.getString(2), rs.getString(3), rs.getBoolean(8), rs.getInt(9),
						rs.getString(10), AdditionalResource.additionalResourcesFromString(rs.getString(11)), score, bonus));
			
			}
			
			return tasks;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getSubmissions(int, int)
	 */
	@Override
	public List<Submission> getSubmissions(int taskID, int groupID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION) +" FROM "+ Ry.Submissions.TABLE
					+" WHERE "+ Ry.Submissions.ID_TASK +"=? AND "+ Ry.Submissions.ID_GROUP +"=?");
			
			st.setInt(1, taskID);
			st.setInt(2, groupID);
			
			rs = st.executeQuery();

			List<Submission> submissions = new ArrayList<Submission>();
			
			while (rs.next()) {
				submissions.add(new Submission(rs.getInt(1), rs.getInt(5), (Integer)rs.getObject(6), rs.getString(7)));
			}
			
			return submissions;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	/**
	 * Convert a table of tasks with scores to a list of TaskSubmissions
	 * The columns expected in the resultSet are:
	 * 1: Submission Id
	 * 2: task id
	 * 3: group id
	 * 4: user id
	 * 5: submit type
	 * 6: intSubmission
	 * 7: textSubmission
	 * 8: score 
	 * 9: bonus
	 * 10: outdated
	 * Submissions for the same task and group are expected to follow each other in the table.
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<TaskSubmissions> convertResultToTaskSubmissions(ResultSet rs, boolean includeRating) throws SQLException {
		List<TaskSubmissions> taskSubmissions = new ArrayList<TaskSubmissions>();
		List<Submission> submissions = null;
		
		int taskID = -1, lastTaskID = -1;
		int groupID = -1, lastGroupID = -1;
		while (rs.next()) {
			lastTaskID = taskID;
			lastGroupID = groupID;
			
			taskID = rs.getInt(2);
			groupID = rs.getInt(3);
			
			
			if (taskID != lastTaskID || groupID != lastGroupID) {
				Integer score = null, bonus = null;
					if (includeRating) {
					score = rs.getInt(8);
					if (rs.wasNull()) score = null;
					bonus = rs.getInt(9);
					if (rs.wasNull()) bonus = null;
				}
				
				boolean scoreOutdated = rs.getBoolean(10);
				
				submissions = new ArrayList<Submission>();
				taskSubmissions.add(new TaskSubmissions(taskID, groupID, submissions, score, bonus, scoreOutdated));
			}
			
			submissions.add(new Submission(rs.getInt(1), rs.getInt(5), (Integer)rs.getObject(6), rs.getString(7)));
		}
		return taskSubmissions;
	}
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getAllSubmissions(int)
	 */
	@Override
	public List<TaskSubmissions> getAllSubmissions(int groupID, boolean includeRating) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION, Ry.Tasks_Groups.SCORE, Ry.Tasks_Groups.BONUS, Ry.Tasks_Groups.OUTDATED) +" FROM "+ Ry.Submissions.TABLE
					+" LEFT JOIN "+Ry.Tasks_Groups.TABLE+" USING("+Ry.Tasks_Groups.ID_GROUP+","+Ry.Tasks_Groups.ID_TASK+") WHERE "+ Ry.Submissions.ID_GROUP +"=? ORDER BY "+ Ry.Submissions.ID_TASK +" ASC");
			
			st.setInt(1, groupID);
			
			rs = st.executeQuery();

			
			
			return convertResultToTaskSubmissions(rs, includeRating);
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	


	@Override
	public List<TaskSubmissions> getSubmissionsByTask(int taskID, boolean includeRating)
			throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION, Ry.Tasks_Groups.SCORE, Ry.Tasks_Groups.BONUS, Ry.Tasks_Groups.OUTDATED) +" FROM "+ Ry.Submissions.TABLE
					+" LEFT JOIN "+Ry.Tasks_Groups.TABLE+" USING("+Ry.Tasks_Groups.ID_GROUP+","+Ry.Tasks_Groups.ID_TASK+") WHERE "+ Ry.Submissions.ID_TASK +"=? ORDER BY "+ Ry.Submissions.ID_GROUP +" ASC");
			
			st.setInt(1, taskID);
			
			rs = st.executeQuery();

			
			
			return convertResultToTaskSubmissions(rs, includeRating);
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	@Override
	public List<TaskSubmissions> getUnratedSubmissions(boolean includeRating) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Submissions.ID, Ry.Submissions.ID_TASK, Ry.Submissions.ID_GROUP,
					Ry.Submissions.ID_USER, Ry.Submissions.SUBMIT_TYPE,
					Ry.Submissions.INT_SUBMISSION, Ry.Submissions.TEXT_SUBMISSION, Ry.Tasks_Groups.SCORE, Ry.Tasks_Groups.BONUS, Ry.Tasks_Groups.OUTDATED) +" FROM "+ Ry.Submissions.TABLE
					+" LEFT JOIN "+Ry.Tasks_Groups.TABLE+" USING("+Ry.Tasks_Groups.ID_GROUP+","+Ry.Tasks_Groups.ID_TASK+") WHERE ("+ Ry.Tasks_Groups.SCORE +" IS NULL AND "+ Ry.Tasks_Groups.BONUS +" IS NULL) OR "+ Ry.Tasks_Groups.OUTDATED +"=1 ORDER BY "+ Ry.Submissions.ID_GROUP +" ASC, "+Ry.Submissions.ID_TASK+" ASC");
			
			rs = st.executeQuery();
			
			return convertResultToTaskSubmissions(rs, includeRating);
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#submit(int, int, int, de.rallye.model.structures.SimpleSubmission)
	 */
	@Override
	public Submission submit(int taskID, int groupID, int userID, SimpleSubmission submission) throws DataException, InputException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			
			
			//Check if the task exists
			st = con.prepareStatement("SELECT * FROM "+ Ry.Tasks.TABLE +" WHERE "+ Ry.Tasks.ID +"=?");
			st.setInt(1, taskID);
			rs = st.executeQuery();
			if (!rs.first())
				throw new InputException("taskID does not exist");
			st.close();
			rs.close();
			
			//TODO: sanity check: see if the submission matches submitType and as such is valid
			

			//Insert into db
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
			
			
			//Invalidate existing score
			st = con.prepareStatement("UPDATE "+Ry.Tasks_Groups.TABLE+" SET outdated = 1 WHERE "+Ry.Tasks_Groups.ID_TASK+"=? AND "+Ry.Tasks_Groups.ID_GROUP+"=?");
			st.setInt(1, taskID);
			st.setInt(2, groupID);
			st.execute();
			
			
			return res;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
	
	private Map<Integer,Node> nodes;
	private List<Edge> edges;
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getNodes()
	 */
	@Override
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
				nodes.put(rs.getInt(1),new Node(rs.getInt(1), rs.getString(2), new LatLng(rs.getDouble(3), rs.getDouble(4)), rs.getString(5)));
			}

		} finally {
			close(con, st, rs);
		}
	}

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getEdges()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getKnownUserAuthorization(int, int, java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getNewUserAuthorization(int, java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#login(int, de.rallye.model.structures.LoginInfo)
	 */
	@Override
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
	
	final Random rnd = new Random();
	private String generateNewUserPassword(int groupID) {
		long pw = System.currentTimeMillis() + groupID + rnd.nextInt();
		
		return Long.toHexString(pw);
	}
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#logout(int, int)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#hasRightsForChatroom(int, int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getChatrooms(int)
	 */
	@Override
	public synchronized List<Chatroom> getChatrooms(int groupID) throws DataException {
		Connection con = null; 
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getChats(int, long, int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#addChat(de.rallye.model.structures.SimpleChatEntry, int, int, int)
	 */
	@Override
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
			} catch (Exception e) {
				logger.error("Exception during Rollback", e);
			}
			
			close(con, st, rs);
		}
	}

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#setPushConfig(int, int, de.rallye.model.structures.PushConfig)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getPushConfig(int, int)
	 */
	@Override
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

			return new PushConfig(rs.getString(2), rs.getString(1));
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	List<PushMode> pushModes;
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getPushModes()
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getAllUsers()
	 */
	@Override
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

	final Map<Integer,List<UserInternal>> members = new HashMap<Integer,List<UserInternal>>();
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getMembers(int)
	 */
	@Override
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
	

	final Map<Integer,List<UserInternal>> roomMembers = new HashMap<Integer,List<UserInternal>>();
	
	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getChatroomMembers(int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#assignNewPictureID(int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#editChatAddPicture(int, int)
	 */
	@Override
	public void editChatAddPicture(int chatID, int pictureID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;
		
		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("UPDATE "+ Ry.Chats.TABLE +" SET "+ Ry.Chats.ID_PICTURE +"=? WHERE "+ Ry.Chats.ID +"=?");
			st.setInt(1, pictureID);
			st.setInt(2, chatID);
			
			if (st.executeUpdate() <= 0)
				throw new DataException("Failed to add picture to Chat");
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#updatePushIds(java.util.HashMap)
	 */
	@Override
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

	@Override
	public int addGroup(Group group) throws DataException {
		//Invalidate cached group list
		groups = null;
		
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			
			con = dataSource.getConnection();
			
			st = con.prepareStatement("INSERT INTO "+ Ry.Groups.TABLE +" ("+ cols(Ry.Groups.NAME, Ry.Groups.DESCRIPTION, Ry.Groups.PASSWORD) +")" +
					" VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
			

			st.setString(1, group.name);
			st.setString(2, group.description);
			st.setString(3, group.password);
			st.execute();
			
			rs = st.getGeneratedKeys();
			
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new DataException("Group could not be created");
			}
			
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	@Override
	public void editGroup(Group group) throws DataException {
		//Invalidate cached group list
		groups = null;
		
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			
			con = dataSource.getConnection();
			
			
			st = con.prepareStatement("UPDATE "+ Ry.Groups.TABLE +" SET "+ Ry.Groups.NAME +"=?, "+ Ry.Groups.DESCRIPTION+"=?, "+Ry.Groups.PASSWORD+"=? WHERE "+Ry.Groups.ID+"=?");

			st.setString(1, group.name);
			st.setString(2, group.description);
			st.setString(3, group.password);
			st.setInt(4, group.groupID);
			st.execute();
			
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
		
	}

	@Override
	public void scoreSubmissions(SubmissionScore[] scores) throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		PreparedStatement delSt = null;
		ResultSet rs = null;
		
		try {
			
			con = dataSource.getConnection();
			
			
			st = con.prepareStatement("INSERT INTO "+ Ry.Tasks_Groups.TABLE +" ("+ cols(Ry.Tasks_Groups.ID_GROUP, Ry.Tasks_Groups.ID_TASK, Ry.Tasks_Groups.SCORE, Ry.Tasks_Groups.BONUS) + ") VALUES  (?,?,?,?) ON DUPLICATE KEY UPDATE "+Ry.Tasks_Groups.SCORE+"=VALUES("+Ry.Tasks_Groups.SCORE+"), "+Ry.Tasks_Groups.BONUS+"=VALUES("+Ry.Tasks_Groups.BONUS+"), "+Ry.Tasks_Groups.OUTDATED+"=false");
			delSt = con.prepareStatement("DELETE FROM "+Ry.Tasks_Groups.TABLE+" WHERE "+Ry.Tasks_Groups.ID_TASK+"=? AND "+Ry.Tasks_Groups.ID_GROUP+"=?");

			for (SubmissionScore score : scores) {
				if (score.remove) {
					delSt.setInt(1, score.taskID);
					delSt.setInt(2, score.groupID);
					delSt.execute();
				} else {
					st.setInt(1, score.groupID);
					st.setInt(2, score.taskID);
					st.setInt(3, score.score);
					st.setInt(4, score.bonus);
					
					st.execute();
				}
			}
			
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(null,delSt,null);
			close(con, st, rs);
		}
	}

	@java.lang.Override
	public RallyeGameState loadGameState() throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.GameState.SHOW_RATING_TO_USERS, Ry.GameState.CAN_SUBMIT)
					+" FROM "+ Ry.GameState.TABLE);
			rs = st.executeQuery();

			if (!rs.first()) //Move to first element
				return null;
			RallyeGameState res = new RallyeGameState(rs.getBoolean(1), rs.getBoolean(2));

			if (rs.next())
				throw new DataException("Database should containt at most 1 GameState");

			return res;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	@java.lang.Override
	public void saveGameState(RallyeGameState gameState) throws DataException {
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Savepoint transaction = null;

		try {
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			transaction = con.setSavepoint();

			st = con.prepareStatement("DELETE FROM "+ Ry.GameState.TABLE);
			st.executeUpdate();
			st.close();

			st = con.prepareStatement("INSERT INTO "+ Ry.GameState.TABLE+" ("+ cols(Ry.GameState.SHOW_RATING_TO_USERS, Ry.GameState.CAN_SUBMIT)
					+") VALUES (?,?)");


			st.setBoolean(1,gameState.isShowRatingToUsers());
			st.setBoolean(2,gameState.isCanSubmit());

			st.executeUpdate();

			con.commit();
		} catch (SQLException e) {
			try {
				con.rollback(transaction);
			} catch (SQLException e1) {
				logger.warn("Exception while rolling back: "+e1);
				e1.printStackTrace();
			}
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	@Override
	public AdminPrincipal getAdminPrincipal(String username, String password) throws DataException, InputException {
		if (username==null || username.length()<=0 || password == null || password.length() <= 0)
			throw new InputException("Incomplete Login");

		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {


			con = dataSource.getConnection();
			st = con.prepareStatement("SELECT "+ cols(Ry.Admins.ID, Ry.Admins.USERNAME, Ry.Admins.RIGHTS)
					+" FROM "+ Ry.Admins.TABLE +" WHERE "+ Ry.Admins.USERNAME +"=? AND "+ Ry.Users.PASSWORD +"=?");
			st.setString(1, username);
			st.setString(2, password);

			rs = st.executeQuery();

			if (rs.first()) {
				int id = rs.getInt(1);
				String rightsStr = rs.getString(3);
				List<String> rights;
				if (rightsStr!=null)
					rights = Arrays.asList(rightsStr.split(","));
				else
					rights = new ArrayList<String>();

				return new AdminPrincipal(id,username,rights);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}

	@Override
	public void editSubmissionAddPicture(int submissionID, int pictureID) throws DataException {
		PreparedStatement st = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = dataSource.getConnection();
			st = con.prepareStatement("UPDATE "+ Ry.Submissions.TABLE +" SET "+ Ry.Submissions.INT_SUBMISSION +"=? WHERE "+ Ry.Submissions.ID +"=?");
			st.setInt(1, pictureID);
			st.setInt(2, submissionID);

			if (st.executeUpdate() > 0)
				return;
			else
				throw new DataException("Failed to add picture to Submission");
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			close(con, st, rs);
		}
	}
}
