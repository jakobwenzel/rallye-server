/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	}

	public WebAppExcept(int status) {
		super(status);
	}

	public WebAppExcept(Response response) {
		super(response);
	}

	public WebAppExcept(Status status) {
		super(status);
	}

	public WebAppExcept(Throwable cause, int status) {
		super(cause, status);
	}

	public WebAppExcept(Throwable cause, Response response) {
		super(cause, response);
	}

	public WebAppExcept(Throwable cause, Status status) {
		super(cause, status);
	}

	public WebAppExcept(Throwable cause) {
		super(cause);
	}
	
	

}
