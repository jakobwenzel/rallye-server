package de.stadtrallye.model;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class DataHandler {

	private static Connection sqlCon;
	private String uri = "localhost";
	private int Port = 10101;

	/**
	 * constructor
	 */
	public DataHandler() {
		// create a new connection if no connection exists
		if (DataHandler.sqlCon == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				sqlCon = java.sql.DriverManager.getConnection("jdbc:mysql://hajoschja.de/rallye?user=felix&password=andro-rallye");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
	}
	
	

	/**
	 * @return the sqlCon
	 */
	protected Connection getSqlCon() {
		return DataHandler.sqlCon;
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
}
