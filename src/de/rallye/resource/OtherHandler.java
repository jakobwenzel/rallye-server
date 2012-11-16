package de.rallye.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.WebApplicationException;

import org.codehaus.jettison.json.JSONObject;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class OtherHandler {

	public static JSONObject getStatus(DataHandler data) {
		Connection con = null;
		Statement stmt = null;
		JSONObject a = new JSONObject();
		ResultSet rs = null;
		try {
			con = data.getSqlCon();

			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT MAX(roundID) as round, UNIX_TIMESTAMP() as serverTime FROM `ry_rounds` LIMIT 1");

			while (rs.next()) {

				a.put("curRound", rs.getInt("round"));
				a.put("serverTime", rs.getString("serverTime"));

			}
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
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
		return a;
	}
}
