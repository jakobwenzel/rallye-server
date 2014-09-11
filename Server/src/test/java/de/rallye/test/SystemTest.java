/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.test;

import de.rallye.test.helper.HttpClient;
import de.rallye.test.helper.StartTestServer;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


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
