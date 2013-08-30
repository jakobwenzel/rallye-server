package de.rallye.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.test.helper.StartTestServer;

@Ignore
public class ClientTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	
	@Test
	public void testIndexStringSecurityContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexSecurityContext() {
		fail("Not yet implemented");
	}

}
