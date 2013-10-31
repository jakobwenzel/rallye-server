package de.rallye.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.ServerInfo;

@Path("rallye/system")
public class System {

	public static final int API_VERSION = 4;
	public static final String API_NAME = "server";

	private final Logger logger = LogManager.getLogger(System.class);
	
	@Inject	RallyeConfig config;
	@Inject	IDataAdapter data;
	
	@GET
	@Path("ping")
	public String ping(@Context HttpHeaders headers) {
		if (headers.getHeaderString("blubbel") != null)
			return "OK (blubbel sent)";
		else
			return "OK (no blubbel)";
	}
	
	@GET
	@Path("picture")
	@Produces("image/jpeg")
	public File getPicture() throws FileNotFoundException {
		File picture = new File(config.getDataDirectory()+"game/picture.jpg");
		if (picture.exists())
			return picture;
		else throw new FileNotFoundException("Picture not found");
	}
	
	@GET
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerInfo getDescription() {
		return config.getServerInfo();
	}

	@GET
	@Path("status")
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		throw new UnsupportedOperationException();//TODO
	}
	
	@GET
	@Path("pushModes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PushMode> getPushModes() throws DataException {
		logger.entry();
	
		List<PushMode> res = data.getPushModes();
		return logger.exit(res);
	}
	
	@GET
	@Path("log")
	@Produces(MediaType.TEXT_HTML)
	public String getLog() throws FileNotFoundException {
		
		File f = new File("log/debug.log");
		
		BufferedReader r = new BufferedReader(new FileReader(f));
		
		@SuppressWarnings("serial")
		LinkedHashMap<Integer, String> list = new LinkedHashMap<Integer, String>() {
			protected boolean removeEldestEntry(java.util.Map.Entry<Integer,String> arg0) {
				return size() > 50;
			}
		};
		
		int i = 0;
		String line;
		
		
		try {
			while ((line = r.readLine()) != null) {
				list.put(i++, line);
			}
		} catch (IOException e) {
			logger.error("Could not read Log", e);
		}
		
		try {
			r.close();
		} catch (IOException e) {
			logger.error("Could not close Log", e);
		}
		
		StringBuilder sb = new StringBuilder("<html><head><title>Log</title></head><body>");
		for (String l: list.values()) {
			sb.append(l).append("<br />");
		}
		return sb.append("</body></html>").toString();
	}
	
	@GET
	@Path("rallye.apk")
	@Produces("application/vnd.android.package-archive")
	public File getApp() throws FileNotFoundException{
		logger.entry();
		File f = new File(config.getDataDirectory()+"rallye.apk");
		if (f.exists())
			return logger.exit(f);
		else throw new FileNotFoundException("Apk not found");
	}
}
