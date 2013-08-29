package de.rallye;

import java.beans.PropertyVetoException;
import java.io.IOException;
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
	
	protected String host = "0.0.0.0";
	protected int port = 10101;
	protected int consolePort = 10100;
	protected String gcmApiKey = "";
	protected String name = "Ray's RallyeServer";
	protected String description = "Mein eigener Testserver, den ich Schritt für Schritt ausbaue bis alles funktioniert";
	protected LatLng[] mapBounds = {new LatLng(49.858959, 8.635107), new LatLng(49.8923691, 8.6746798)};
	protected LatLng mapCenter = new LatLng(49.877648, 8.651762);

	static class DbData {
		public String connectString;
		public String username;
		public String password;
		public int maxIdleTime = 3600;
	}
	protected DbData dbData = new DbData();
	
	protected String dataDirectory = "./";
	protected boolean dataRelative = false;
	
	//Unchangeable data
	protected final ServerInfo.Api[] APIS = {new ServerInfo.Api("ist_rallye", 1), new ServerInfo.Api("scotlandYard", 3), new ServerInfo.Api("server", 4)};

	
	protected final String build;
	
	public DataAdapter getMySQLDataAdapter() throws SQLException {
		// create dataBase Handler
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			logger.catching(e);
		}
		dataSource.setJdbcUrl(dbData.connectString);
		dataSource.setUser(dbData.username);
		dataSource.setPassword(dbData.password);
		dataSource.setMaxIdleTime(dbData.maxIdleTime); // set max idle time to 1 hour
		
		DataAdapter da = new DataAdapter(dataSource);
		
		return da;
	}

	
	public RallyeConfig() {
		GitRepositoryState git;
		String build;
		try {
			git = GitRepositoryState.getGitRepositoryState();
			build = git.getDescribe()+", "+git.getBuildTime();
		} catch (IOException e) {
			build = "No build info";
		}
		this.build = build;
	}
	
	private String configFileDir = "";
	public void setConfigFileDir(String dir) {
		this.configFileDir = dir;
	}
	
	@JsonIgnore
	public ImageRepository getImageRepository() {
		return new ImageRepository(getDataDirectory()+"pics/", 100, 25);
	}
	
	public String getDataDirectory() {
		if (dataRelative) {
			return configFileDir+dataDirectory;
		} else {
			return dataDirectory;
		}
	}
	
	public String getRawDataDirectory() {
		return dataDirectory;
	}
	
	public boolean getDataRelative() {
		return dataRelative;
	}
	
	public String getHostName() {
		return host;
	}
	
	public int getRestPort() {
		return port;
	}
	
	public int getConsolePort() {
		return consolePort;
	}
	
	public String getGcmKey() {
		return gcmApiKey;
	}

	public List<LatLng> getMapBounds() {
		List<LatLng> res = new ArrayList<LatLng>();
		for (LatLng ll : mapBounds) {
			res.add(ll);
		}
		return res;
	}
	
	public LatLng getMapCenter() {
		return mapCenter;
	}
	
	@JsonIgnore
	public ServerInfo getServerDescription() {
		return new ServerInfo(name, description, APIS, build);
	}

}
