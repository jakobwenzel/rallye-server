package de.rallye;

import java.awt.image.DataBufferDouble;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.LatLng;
import de.rallye.model.structures.ServerInfo;

public class RallyeConfig {
	
	private static final Logger logger = LogManager.getLogger(RallyeConfig.class);
	
	//This is the default data.
	
	protected String HOST = "0.0.0.0";
	protected int PORT = 10101;
	protected int CONSOLE_PORT = 10100;
	protected String GCM_API_KEY = "AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0";
	protected String NAME = "Ray's RallyeServer";
	protected String DESCRIPTION = "Mein eigener Testserver, den ich Schritt für Schritt ausbaue bis alles funktioniert";
	protected LatLng[] MAP_BOUNDS = {new LatLng(49.858959, 8.635107), new LatLng(49.8923691, 8.6746798)};
	protected LatLng MAP_CENTER = new LatLng(49.877648, 8.651762);

	static class DbData {
		public String connectString;
		public String username;
		public String password;
		public int maxIdleTime = 3600;
	}
	protected DbData DB_DATA = new DbData();
	
	//Unchangeable data
	protected final ServerInfo.Api[] APIS = {new ServerInfo.Api("ist_rallye", 1), new ServerInfo.Api("scotlandYard", 3), new ServerInfo.Api("server", 4)};

	
	public DataAdapter getMySQLDataAdapter() throws SQLException {
		// create dataBase Handler
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			logger.catching(e);
		}
		dataSource.setJdbcUrl(DB_DATA.connectString);
		dataSource.setUser(DB_DATA.username);
		dataSource.setPassword(DB_DATA.password);
		dataSource.setMaxIdleTime(DB_DATA.maxIdleTime); // set max idle time to 1 hour
		
		DataAdapter da = new DataAdapter(dataSource);
		
		return da;
	}

	@JsonIgnore
	public ImageRepository getImageRepository() {
		return new ImageRepository("pics/", 100, 25);
	}
	
	public String getHostName() {
		return HOST;
	}
	
	public int getRestPort() {
		return PORT;
	}
	
	public int getConsolePort() {
		return CONSOLE_PORT;
	}
	
	public String getGcmKey() {
		return GCM_API_KEY;
	}

	public List<LatLng> getMapBounds() {
		List<LatLng> res = new ArrayList<LatLng>();
		for (LatLng ll : MAP_BOUNDS) {
			res.add(ll);
		}
		return res;
	}
	
	public LatLng getMapCenter() {
		return MAP_CENTER;
	}
	
	@JsonIgnore
	public ServerInfo getServerDescription() {
		return new ServerInfo(NAME, DESCRIPTION, APIS);
	}

}
