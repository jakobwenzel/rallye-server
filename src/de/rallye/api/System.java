package de.rallye.api;

import java.util.List;

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

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.auth.KnownUserAuth;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.ServerConfig;

@Path("rallye/system")
public class System {

	private static Logger logger = LogManager.getLogger(System.class);
	
	private RallyeResources R = RallyeServer.getResources();

	@GET
	@Path("status")
	@ResourceFilters(KnownUserAuth.class)
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
			ServerConfig res = R.data.getServerConfig();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getConfig failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("pushModes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PushMode> getPushModes() {
		logger.entry();
		
		try {
			List<PushMode> res = R.data.getPushModes();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getPushModes failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
