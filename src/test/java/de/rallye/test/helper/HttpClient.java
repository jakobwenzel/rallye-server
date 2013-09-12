package de.rallye.test.helper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClient {
	static DefaultHttpClient httpclient = new DefaultHttpClient();

	//TODO Add auth
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public static List<String> apiCallAsString(String url, int expectedStatusCode) throws IOException {
		HttpResponse r = apiCall(url,expectedStatusCode);
		return IOUtils.readLines(r.getEntity().getContent());
	}

}
