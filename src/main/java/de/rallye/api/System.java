package de.rallye.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.RallyeConfig;
import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.auth.KnownUserAuth;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.LatLng;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.ServerConfig;
import de.rallye.model.structures.ServerInfo;

@Path("rallye/system")
public class System {

	private static Logger logger = LogManager.getLogger(System.class);
	
	private RallyeResources R = RallyeServer.getResources();
	
	@GET
	@Path("ping")
	public String ping() {
		return "OK";
	}
	
	@GET
	@Path("picture")
	@Produces("image/jpeg")
	public File getPicture() {
		return new File("game/picture.jpg");
	}
	
	@GET
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerInfo getDescription() {
		return RallyeConfig.getServerDescription();
	}

	@GET
	@Path("status")
	@ResourceFilters(KnownUserAuth.class)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		throw new UnsupportedOperationException();//TODO
	}
	
	@GET
	@Path("mapBounds")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LatLng> getBounds() {
		logger.entry();
		
		return RallyeConfig.getMapBounds();
	}
	
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerConfig getConfig() {
		logger.entry();
		
		try {
			ServerConfig res = R.data.getServerConfig();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getConfig failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("pushModes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PushMode> getPushModes() {
		logger.entry();
		
		try {
			List<PushMode> res = R.data.getPushModes();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getPushModes failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
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
			};
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
	public File getApp() {
		logger.entry();
		File f = new File("rallye.apk");
		
		return logger.exit(f);
	}
}
