/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

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
