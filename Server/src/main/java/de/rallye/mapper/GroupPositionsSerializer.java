package de.rallye.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.Node;

import java.io.IOException;
import java.util.Map;

public class GroupPositionsSerializer extends JsonSerializer<Map<Integer,Node>> {

	@Override
	public void serialize(Map<Integer, Node> value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException/*,
			JsonProcessingException*/ {
		
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
