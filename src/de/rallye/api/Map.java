package de.rallye.api;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.Node;

@Path("rallye/map")
public class Map {
	
	private Logger logger =  LogManager.getLogger(Map.class.getName());

	private RallyeResources R = RallyeServer.getResources();

	@GET
	@Path("nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Node> getNodes() {
		logger.entry();
		
		Collection<Node> res = R.data.getNodes().values();
		return logger.exit(res);
	}
	
	@GET
	@Path("edges")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Edge> getEdges() {
		logger.entry();
		
		List<Edge> res = R.data.getEdges();
		return logger.exit(res);
		
	}
}
