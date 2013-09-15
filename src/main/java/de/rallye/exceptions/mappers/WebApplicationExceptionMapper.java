package de.rallye.exceptions.mappers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class WebApplicationExceptionMapper implements
		ExceptionMapper<WebApplicationException> {

	private static final Logger logger = LogManager.getLogger(WebApplicationExceptionMapper.class);
	@Override
	public Response toResponse(WebApplicationException exception) {
		logger.info("Mapping exception");
		logger.info(exception.getResponse());
		exception.printStackTrace();
		return exception.getResponse();
	}

}
