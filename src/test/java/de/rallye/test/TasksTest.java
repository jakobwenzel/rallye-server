package de.rallye.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.test.helper.StartTestServer;

@Ignore
public class TasksTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	
	@Test
	public void testGetTasks() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSubmissions() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllSubmissions() {
		fail("Not yet implemented");
	}

	@Test
	public void testSubmit() {
		fail("Not yet implemented");
	}

}
