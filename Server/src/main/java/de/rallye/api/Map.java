package de.rallye.api;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.MapConfig;
import de.rallye.model.structures.Node;

@Path("rallye/map")
public class Map {
	
	private static final Logger logger =  LogManager.getLogger(Map.class.getName());

	@Inject	IDataAdapter data;
	@Inject	RallyeConfig config;

	@GET
	@Path("nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Node> getNodes() {
		logger.entry();
		
		Collection<Node> res = data.getNodes().values();
		return logger.exit(res);
	}
	
	@GET
	@Path("edges")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Edge> getEdges() {
		logger.entry();
		
		List<Edge> res = data.getEdges();
		return logger.exit(res);
	}

	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public MapConfig getConfig() {
		logger.entry();

		MapConfig res = config.getMapConfig();
		return logger.exit(res);
	}
}