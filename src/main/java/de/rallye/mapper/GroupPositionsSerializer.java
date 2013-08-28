package de.rallye.mapper;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import de.rallye.model.structures.Group;
import de.rallye.model.structures.Node;

public class GroupPositionsSerializer extends JsonSerializer<Map<Integer,Node>> {

	@Override
	public void serialize(Map<Integer, Node> value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		
		jgen.writeStartArray();
		
		for(Integer groupId : value.keySet()) {
			int nodeId = value.get(groupId).nodeID;
				
			jgen.writeStartObject();
			
			jgen.writeFieldName(Group.GROUP_ID);
			jgen.writeNumber(groupId);
			
			jgen.writeFieldName(Node.NODE_ID);
			jgen.writeNumber(nodeId);
			
			jgen.writeEndObject();
			
			
		}
		
		jgen.writeEndArray();
		
	}

}
