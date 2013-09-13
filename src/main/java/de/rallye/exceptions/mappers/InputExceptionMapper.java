package de.rallye.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.rallye.exceptions.InputException;

@Provider
public class InputExceptionMapper implements ExceptionMapper<InputException>{

	@Override
	public Response toResponse(InputException exception) {
		return Response.status(400).entity(exception.getMessage()).build();
	}
	

}
