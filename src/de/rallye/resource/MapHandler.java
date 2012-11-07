package de.rallye.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class MapHandler {

	// DataHandler dh = null;

	/**
	 * constructor
	 * 
	 * @param #
	 */
	/*
	 * public MapHandler(DataHandler dataHandler) { this.dh = dataHandler; }
	 */

	public static JSONArray getAllNodes(DataHandler data) {
		Connection con = null;
		java.sql.Statement stmt = null;
		JSONArray lst = new JSONArray();
		ResultSet rs = null;

		try {
			con = data.getSqlCon();

			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT NodeID,name,lat,lon,description FROM ry_nodes");

			while (rs.next()) {

				JSONObject a = new JSONObject();

				a.put("nodeID", rs.getInt("NodeID"));
				a.put("name", rs.getString("name"));
				a.put("lat", rs.getDouble("lat"));
				a.put("lon", rs.getDouble("lon"));
				a.put("description", rs.getString("description"));

				lst.put(a);
			}
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e1) {
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
			throw new WebApplicationException(500);
		}
		return lst;
	}
}
