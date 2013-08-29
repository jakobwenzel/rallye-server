package de.rallye.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author Felix H�bner
 * @version 1.0
 *
 */
public class WebAppExcept extends WebApplicationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7076560597350579427L;
	private final static int defaultStatus = 500;
	
	/**
	 * Construct a new instance using the supplied message 
	 * @param message - message that will be returned to the client
	 */
	public WebAppExcept(String message) {
		super(Response.status(defaultStatus).entity(message).build());
	}
	
	/**
	 * Construct a new instance using the supplied message and specified HTTP status code
	 * @param status - the HTTP status code that will be returned to the client
	 * @param message - message that will be returned to the client
	 */
	public WebAppExcept(String message, int status) {
		super(Response.status(status).entity(message).build());
	}
	
	/**
	 * Construct a new instance using the supplied message
	 * @param cause - the underlying cause of the exception
	 * @param message - message that will be returned to the client
	 */
	public WebAppExcept(java.lang.Throwable cause, String message) {
		super(cause,Response.status(defaultStatus).entity(message).build());
	}
	
	/**
	 * Construct a new instance using the supplied message and specified HTTP status code
	 * @param cause - the underlying cause of the exception
	 * @param message - message that will be returned to the client
	 * @param status - the HTTP status code that will be returned to the client
	 */
	public WebAppExcept(java.lang.Throwable cause, String message, int status) {
		super(cause,Response.status(status).entity(message).build());
	}
	
	

	public WebAppExcept() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(int status) {
		super(status);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Response response) {
		super(response);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Status status) {
		super(status);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Throwable cause, int status) {
		super(cause, status);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Throwable cause, Response response) {
		super(cause, response);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Throwable cause, Status status) {
		super(cause, status);
		// TODO Auto-generated constructor stub
	}

	public WebAppExcept(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	

}
