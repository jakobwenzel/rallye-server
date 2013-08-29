package de.rallye.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ramon
 * Date: 30.08.13
 * Time: 00:18
 * To change this template use File | Settings | File Templates.
 */
public class EnsureMimeType implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		if (!responseContext.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE))
			responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, "");
	}
}
