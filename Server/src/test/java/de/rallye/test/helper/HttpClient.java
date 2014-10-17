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

package de.rallye.test.helper;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HttpClient {
	static DefaultHttpClient httpclient = new DefaultHttpClient();

	//TODO Add auth
	public static HttpResponse apiCall(String url, int expectedStatusCode)
			throws IOException {
		HttpGet httpget = new HttpGet("http://localhost:10111/" + url);
		HttpResponse r = httpclient.execute(httpget);
		int statusCode = r.getStatusLine().getStatusCode();
		if (expectedStatusCode != statusCode) {
			System.err.println("Got unexpected status code: " + statusCode);
			// Let's see if it's printable
			boolean printable = true;
			Header[] ct = r.getHeaders("Content-Type");
			if (ct.length > 0) {
				String type = ct[0].getValue();
				printable = (type.startsWith("text/") || type
						.startsWith("application/"));
			}

			if (printable) {
				System.err.println("This is the content:");
				List<String> contents = IOUtils.readLines(r.getEntity().getContent());
				for (String line: contents) 
					System.err.println(line);
			}

			assertEquals("StatusCode should match expected",
					expectedStatusCode, statusCode);

		}
		return r;
	}
	

	public static List<String> apiCallAsString(String url, int expectedStatusCode) throws IOException {
		HttpResponse r = apiCall(url,expectedStatusCode);
		return IOUtils.readLines(r.getEntity().getContent());
	}

}
