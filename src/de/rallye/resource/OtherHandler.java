package de.rallye.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.rallye.model.ScottlandYardRallye;


/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class OtherHandler {
	
	public static JSONObject getStatus(DataHandler data) {
		Connection con = data.getSqlCon();
		java.sql.Statement stmt;
		JSONObject a = new JSONObject();
		
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(roundID) as round, UNIX_TIMESTAMP() as serverTime FROM `ry_rounds` LIMIT 1");
			try {
				while (rs.next()) {

					

					a.put("curRound", rs.getInt("round"));
					a.put("serverTime", rs.getString("serverTime"));
					
				}
			} catch (JSONException e) {
				//this exception should not happen because all Keys are not ""
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return a;
	}
}
