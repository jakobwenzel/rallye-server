package de.rallye.test;

import static de.rallye.test.helper.HttpClient.apiCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.exceptions.DataException;
import de.rallye.model.structures.Group;
import de.rallye.test.db.MockDataAdapter;
import de.rallye.test.helper.StartTestServer;

public class GroupsTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	

	static ObjectMapper mapper = new ObjectMapper();
	static MockDataAdapter data = MockDataAdapter.getInstance();
	
	@Test 
	public void testGetGroups() throws IOException, DataException {
		StartTestServer.getServer();
		
		HttpResponse c = apiCall("rallye/groups", 200);
		List<Group> groups = mapper.readValue(c.getEntity().getContent(), new TypeReference<List<Group>>(){});
		assertEquals("Returned Groups should be equal to source",data.getGroups(),groups);
	}

	@Test @Ignore
	public void testGetMembers() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetGroupAvatar() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetUserInfo() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetPushSettings() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testSetPushConfig() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testLogin() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testLogout() {
		fail("Not yet implemented");
	}

}
