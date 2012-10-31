package de.stadtrallye.model;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class DataHandler {

	private Connection con = null;
	
	private String uri = "localhost";
	private int Port = 10101;
	
	/**
	 * @return the con
	 */
	public Connection getCon() {
		return con;
	}

	
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}


	/**
	 * @return the port
	 */
	public int getPort() {
		return Port;
	}


	public DataHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			con = java.sql.DriverManager.getConnection("jdbc:mysql://hajoschja.de?user=felix&password=andro-rallye");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}
}
