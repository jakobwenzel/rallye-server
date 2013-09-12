package de.rallye.api;

import de.rallye.config.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.MapConfig;
import de.rallye.model.structures.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;

@Path("rallye/map")
public class Map {
	
	private Logger logger =  LogManager.getLogger(Map.class.getName());

	@Inject	DataAdapter data;
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

		//TODO: from GameConfig?
		MapConfig res = config.getMapConfig();
		return logger.exit(res);
	}
}