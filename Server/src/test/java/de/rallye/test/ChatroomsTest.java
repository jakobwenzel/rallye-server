package de.rallye.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.test.helper.StartTestServer;

@Ignore
public class ChatroomsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	
	@Test
	public void testGetChatrooms() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChatsIntLongSecurityContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChatsIntSecurityContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddChat() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMembers() {
		fail("Not yet implemented");
	}

}
