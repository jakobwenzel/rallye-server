package de.rallye.db;

public class Ry {
	
	public static class PushModes {
		public static final String TABLE = "ry_pushModes";
		public static final String ID = "pushModeID";
		public static final String NAME = "name";
	}
	
	public static class Groups {
		public static final String TABLE = "ry_groups";
		public static final String ID = "groupID";
		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String PASSWORD = "password";
	}
	
	public static class Users {
		public static final String TABLE = "ry_clients";
		public static final String ID = "clientID";
		public static final String ID_GROUP = Groups.ID;
		public static final String PUSH_ID = "pushID";
		public static final String ID_PUSH_MODE = PushModes.ID;
		public static final String UNIQUE_ID = "uniqueID";
		public static final String PASSWORD = "password";
		public static final String NAME = "name";
	}

	public static class Nodes {
		public static final String TABLE = "ry_nodes";
		public static final String ID = "nodeID";
		public static final String NAME = "name";
		public static final String LAT = "lat";
		public static final String LON = "lon";
		public static final String DESCRIPTION = "description";
	}
	
	public static class Edges {
		public static final String TABLE = "ry_edges";
		public static final String A = "nodeA";
		public static final String B = "nodeB";
		public static final String TYPE = "type";
	}
	
	public static class Chatrooms {
		public static final String TABLE = "ry_chatrooms";
		public static final String ID = "chatroomID";
		public static final String NAME = "name";
	}
	
	public static class Groups_Chatrooms {
		public static final String TABLE = "ry_chatrooms_groups";
		public static final String ID_GROUP = Groups.ID;
		public static final String ID_CHATROOM = Chatrooms.ID;
	}
	
	public static class Messages {
		public static final String TABLE = "ry_messages";
		public static final String ID = "messageID";
		public static final String MSG = "message";
	}
	
	public static class Pictures {
		public static final String TABLE = "ry_pictures";
		public static final String ID = "pictureID";
		public static final String ID_USER = Users.ID;
		public static final String TIMESTAMP = "timestamp";
	}
	
	public static class Chats {
		public static final String TABLE = "ry_chats";
		public static final String ID = "chatID";
		public static final String TIMESTAMP = "timestamp";
		public static final String ID_USER = Users.ID;
		public static final String ID_GROUP = Groups.ID;
		public static final String ID_MESSAGE = Messages.ID;
		public static final String ID_PICTURE = Pictures.ID;
		public static final String ID_CHATROOM = Chatrooms.ID;
	}
	
	public static class Tasks {
		public static final String TABLE = "ry_tasks";
		public static final String ID = "taskID";
		public static final String NAME = "taskName";
		public static final String DESCRIPTION = "description";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String LOCATION_SPECIFIC = "locationSpecific";
		public static final String RADIUS = "radius";
		public static final String MULTIPLE_SUBMITS = "multipleSubmits";
		public static final String SUBMIT_TYPE = "submitType";
		public static final String POINTS = "points";
//		public static final String ID_PICTURE = Pictures.ID;
		public static final String ADDITIONAL_RESOURCES = "additionalResources";
	}
	
	public static class Submissions {
		public static final String TABLE = "ry_submissions";
		public static final String ID = "submissionID";
		public static final String ID_TASK = Tasks.ID;
		public static final String ID_GROUP = Groups.ID;
		public static final String ID_USER = Users.ID;
		public static final String SUBMIT_TYPE = "submitType";
		public static final String SCORE = "score";
		public static final String INT_SUBMISSION = "intSubmission";
		public static final String TEXT_SUBMISSION = "textSubmission";
	}
	
	@Deprecated //TODO: specific for Scotland Yard, not modular
	public static class Config {
		public static final String TABLE = "ry_config";
		public static final String ID = "configID";
		public static final String TYPE = "gameType";
		public static final String NAME = "gameName";
		public static final String LAT = "location_lat";
		public static final String LON = "location_lon";
		public static final String TICKETS_BIKE = "tickets_bike";
		public static final String TICKETS_FOOT = "tickets_foot";
		public static final String TICKETS_TRAM = "tickets_tram";
		public static final String TICKETS_BUS = "tickets_bus";
		public static final String ROUNDS = "rounds";
		public static final String ROUND_TIME = "roundTime";
		public static final String START_TIME = "gameStartTime";
		public static final String SPAWN_ANYWHERE = "freeStartPoint";
	}
}
