package de.rallye.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.DataException;

@Provider
public class DataExceptionMapper  implements
ExceptionMapper<DataException>{

    private static final Logger logger = LogManager.getLogger(DataExceptionMapper.class);
	@Override
	public Response toResponse(DataException exception) {
		logger.error(exception);
		exception.printStackTrace();
		return Response.serverError().entity("Internal server error. A DataExeption occurred.").build();
	}

}
