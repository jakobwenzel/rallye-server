package de.rallye.model.structures;

import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.EdgeNotFoundException;
import de.rallye.exceptions.NodeNotFoundException;
import de.rallye.exceptions.NodeOccupiedException;
import de.rallye.mapper.GroupPositionsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
@Service
public class GameState {

	private final DataAdapter data;
	
	public int roundNumber = 0;
	
	@JsonSerialize(using=GroupPositionsSerializer.class)
	public Map<Integer,Node> positions = new HashMap<Integer,Node>();

	@JsonSerialize(using=GroupPositionsSerializer.class)
	public Map<Integer,Node> upcomingPositions = new HashMap<Integer,Node>();

	@Inject
	public GameState(DataAdapter data) {
		this.data = data;
	}


	private int getNodeOccupant(Node node, Map<Integer,Node> pos) {
		for (Integer i: pos.keySet()) {
			Node n = pos.get(i);
			if (n.equals(node)) return i;
		}
		return -1;
	}
	
	/**
	 * Returns the group that has registered this node as upcoming position
	 * @param node
	 * @return The group's id or -1 if none.
	 */
	public int upcomingNodeOccupant(Node node) {
		return getNodeOccupant(node,upcomingPositions);
	}
	
	/**
	 * Returns the group that is currently at this node
	 * @param node
	 * @return The group's id or -1 if none.
	 */
	public int currentNodeOccupant(Node node) {
		return getNodeOccupant(node,positions);
	}
	
	/**
	 * Set the next position a group want to go to
	 * @param groupId
	 * @param nodeID
	 */
	public void setUpcomingPosition(int groupId, int nodeID) throws NodeNotFoundException, NodeOccupiedException, EdgeNotFoundException, DataException {
		
		//Check if dest node is valid
		Node destNode;
		try {
			destNode = data.getNodes().get(nodeID);
		} catch(Exception e) {
			throw new NodeNotFoundException();
		}
		assert(destNode.nodeID==nodeID);

		Node currentNode = positions.get(groupId);
		
		for (Edge edge: destNode.getEdges()) {
			if (edge.getOtherNode(destNode).equals(currentNode)) {
				
				int upcomingOccupant = upcomingNodeOccupant(destNode);
				if (upcomingOccupant!=-1 && upcomingOccupant!=groupId) {
					throw new NodeOccupiedException();
				}
				int currentOccupant = currentNodeOccupant(destNode);
				if (currentOccupant!=-1 && currentOccupant!=groupId && !upcomingPositions.containsKey(currentOccupant)) {
					throw new NodeOccupiedException();
				}

				upcomingPositions.put(groupId, destNode);
				
				return;
			}
		}
		throw new EdgeNotFoundException();
	}
	
	public void nextRound() {
		
		//Did some group forget to register a position
		Collection<Integer> unregistered = positions.keySet();
		unregistered.removeAll(upcomingPositions.keySet());
		
		//Generate new positions for unregistered groups
		for (Integer group : unregistered) {
			Node current = positions.get(group);
			//TODO: Generate random valid new position
			upcomingPositions.put(group, current);
		}
		
		//Switch positions
		positions = upcomingPositions;
		upcomingPositions = new HashMap<Integer,Node>();
		
		//TODO: Notify clients
		
		
	}

}
