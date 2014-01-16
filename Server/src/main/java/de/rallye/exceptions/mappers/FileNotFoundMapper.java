package de.rallye.exceptions.mappers;

import java.io.FileNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class FileNotFoundMapper implements
ExceptionMapper<FileNotFoundException> {

	@Override
	public Response toResponse(FileNotFoundException exception) {
		
		return Response.status(404).type(MediaType.TEXT_HTML).entity(exception.getMessage()).build();
	}
}
