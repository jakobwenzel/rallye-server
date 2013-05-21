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

import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Node;
import de.rallye.model.structures.PrimitiveEdge;

@Path("/StadtRallye/map")
public class Map {
	
	private Logger logger =  LogManager.getLogger(Map.class.getName());

	private DataAdapter data = GameHandler.data;//TODO: get it _NOT_ from gameHandler (perhaps inject using Guice??)

	@GET
	@Path("nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Node> getNodes() {
		logger.entry();
		
		try {
			List<Node> res = data.getNodes();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getNodes failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("edges")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PrimitiveEdge> getEdges() {
		logger.entry();
		
		try {
			List<PrimitiveEdge> res = data.getEdges();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getEdges failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
