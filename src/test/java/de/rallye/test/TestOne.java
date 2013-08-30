/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rallye.test;

import static de.rallye.test.helper.HttpClient.apiCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.RallyeServer;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Group;
import de.rallye.test.db.MockDataAdapter;
import de.rallye.test.helper.StartTestServer;
/**
 * 
 * @author Jakob Wenzel
 */
@Ignore
public class TestOne {

	static ObjectMapper mapper = new ObjectMapper();

	static MockDataAdapter data = MockDataAdapter.getInstance();
	@Test
	public void getServer() {
		RallyeServer server = StartTestServer.getServer();
		assertNotNull("Server should not be null", server);
	}

	@Test
	public void getSystemInfo() throws IOException, DataException {
		StartTestServer.getServer();
		
		HttpResponse c = apiCall("rallye/groups", 200);
		List<Group> groups = mapper.readValue(c.getEntity().getContent(), new TypeReference<List<Group>>(){});
		assertEquals("Returned Groups should be equal to source",data.getGroups(),groups);
	}
}
