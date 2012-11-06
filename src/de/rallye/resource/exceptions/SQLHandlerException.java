package de.rallye.resource.exceptions;

import java.sql.SQLException;

/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class SQLHandlerException extends SQLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1079484133258775900L;

	/**
	 * 
	 */
	public SQLHandlerException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SQLHandlerException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SQLHandlerException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SQLHandlerException(String arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SQLHandlerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public SQLHandlerException(String arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public SQLHandlerException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public SQLHandlerException(String arg0, String arg1, int arg2, Throwable arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
