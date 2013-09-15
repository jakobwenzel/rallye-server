package de.rallye.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.InputException;

@Provider
public class InputExceptionMapper implements ExceptionMapper<InputException>{

    private static final Logger logger = LogManager.getLogger(InputExceptionMapper.class);
	@Override
	public Response toResponse(InputException exception) {
		logger.error(exception);
		exception.printStackTrace();
		return Response.status(400).entity(exception.getMessage()).build();
	}
	

}
