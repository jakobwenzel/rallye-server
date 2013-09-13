package de.rallye.exceptions.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.rallye.exceptions.DataException;

@Provider
public class DataExceptionMapper  implements
ExceptionMapper<DataException>{

	@Override
	public Response toResponse(DataException exception) {
		return Response.serverError().entity("Internal server error. A DataExeption occurred.").build();
	}

}
