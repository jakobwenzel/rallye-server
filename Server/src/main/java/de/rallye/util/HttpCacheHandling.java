package de.rallye.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Created by Ramon on 20.09.2014.
 */
public class HttpCacheHandling {

	public static void checkModifiedSince(Request request, long since) throws WebApplicationException {
		Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(new Date(since));
		if (responseBuilder != null)
			throw new WebApplicationException(responseBuilder.build());
	}
}
