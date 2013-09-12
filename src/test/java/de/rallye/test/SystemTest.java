package de.rallye.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.test.helper.HttpClient;
import de.rallye.test.helper.StartTestServer;


public class SystemTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	
	@Test
	public void testPing() throws IOException {
		List<String> r = HttpClient.apiCallAsString("rallye/system/ping", 200);
		assertEquals("Ping should return one line",1,r.size());
		assertTrue("Ping should return OK",r.get(0).startsWith("OK"));
	}

	@Test @Ignore
	public void testGetPicture() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetDescription() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testProduceError() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetStatus() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetBounds() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetConfig() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetPushModes() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetLog() {
		fail("Not yet implemented");
	}

	@Test @Ignore
	public void testGetApp() {
		fail("Not yet implemented");
	}

}
