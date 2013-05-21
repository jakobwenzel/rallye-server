package de.rallye.db;

public class Ry {
	
	
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
		public static final String FOREIGN_GROUP = Groups.ID;
		public static final String GCM = "gcmRegID";
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
}
