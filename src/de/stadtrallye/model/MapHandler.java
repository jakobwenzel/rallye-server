package de.stadtrallye.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	public static JSONArray getAllNodes(DataHandler dh) {
		Connection con = dh.getSqlCon();
		java.sql.Statement stmt;
		JSONArray lst = new JSONArray();
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT NodeID,name,lat,lon,description FROM ry_nodes");
			try {
				while (rs.next()) {

					JSONObject a = new JSONObject();

					a.put("nodeID", rs.getInt("NodeID"));
					a.put("name", rs.getString("name"));
					a.put("lat", rs.getDouble("lat"));
					a.put("lon", rs.getDouble("lon"));
					a.put("description", rs.getString("description"));

					lst.put(a);
				}
			} catch (JSONException e) {
				//this exception should not happen because all Keys are not ""
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return lst;
	}
}
