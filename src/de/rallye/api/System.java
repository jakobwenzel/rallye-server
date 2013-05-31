package de.rallye.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.auth.AuthFilter;
import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ServerConfig;

@Path("system")
public class System {

	private static Logger logger = LogManager.getLogger(System.class);
	
	private DataAdapter data = GameHandler.data;//TODO: get it _NOT_ from gameHandler (perhaps inject using Guice??)

	@GET
	@Path("status")
	@ResourceFilters(AuthFilter.class)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		throw new NotImplementedException();//TODO
	}
	
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerConfig getConfig() {
		logger.entry();
		
		try {
			ServerConfig res = data.getServerConfig();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getConfig failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
