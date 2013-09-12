package de.rallye.test;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.rallye.test.helper.StartTestServer;

@Ignore
public class PicsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StartTestServer.getServer(); //Start a server
	}
	
	@Test
	public void testUploadPictureWithHash() {
		fail("Not yet implemented");
	}

	@Test
	public void testUploadPicture() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPictureInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPictureIntPictureSizeString() {
		fail("Not yet implemented");
	}

}
