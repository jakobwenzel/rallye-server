package de.rallye;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.rallye.api.Game;
import de.rallye.api.Tasks;
import de.rallye.api.System;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.LatLng;
import de.rallye.model.structures.ServerInfo;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

public class RallyeConfig {
	
	private static final Logger logger = LogManager.getLogger(RallyeConfig.class);
	
	//This is the default data.
	@JsonProperty protected String hostName = "0.0.0.0";
	@JsonProperty protected int restPort = 10101;
	@JsonProperty protected int consolePort = 10100;
	@JsonProperty protected String gcmApiKey = "";
	@JsonProperty protected String serverName = "";
	@JsonProperty protected String description = "";
	@JsonProperty protected LatLng[] mapBounds = {};
	@JsonProperty protected LatLng mapCenter;

	@JsonProperty private String configFileDir = "";

	public static class DbConnectionConfig {
		public String connectString;
		public String username;
		public String password;
		public int maxIdleTime = 3600;
	}

	@JsonProperty protected DbConnectionConfig dbConnectionConfig = new DbConnectionConfig();
	@JsonProperty protected String dataDirectory = "data/";
	@JsonProperty protected boolean dataRelativeToConfig = false;

	//TODO: read Api versions from modules / manifest
	protected final ServerInfo.Api[] APIS = {new ServerInfo.Api(Tasks.API_NAME, Tasks.API_VERSION), new ServerInfo.Api(Game.API_NAME, Game.API_VERSION), new ServerInfo.Api(System.API_NAME, System.API_VERSION)};
	protected final String build;

	
	public RallyeConfig() {
		GitRepositoryState git;
		String build;
		try {
			git = GitRepositoryState.getGitRepositoryState();
			build = git.getDescription()+", "+git.getBuildTime();
		} catch (IOException e) {
			build = "No build info";
		}
		this.build = build;
	}

	/**
	 * Read Config from Config File if present
	 * @return Found Config File or Default Config
	 */
	public static RallyeConfig fromFile(File configFile) {
		if (configFile==null) {
			logger.warn("No config file. Using Default Config");
			return new RallyeConfig();
		}
		logger.info("Loading config file from {}/{}", configFile.getParent(), configFile);
		ObjectMapper mapper = new ObjectMapper();
		try {
			RallyeConfig config = mapper.readValue(configFile, RallyeConfig.class);
			File parent = configFile.getParentFile();
			if (parent!=null)
				config.setConfigFileDir(parent+File.separator);
			else
				config.setConfigFileDir("");
				
			logger.debug(config.toString());
			return config;
		} catch ( IOException e) {
			logger.error("Falling back to default config", e);
			return new RallyeConfig();
		}
	}

	public DataAdapter getMySQLDataAdapter() throws SQLException {
		// create dataBase Handler
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			logger.catching(e);
		}

		dataSource.setJdbcUrl(dbConnectionConfig.connectString);
		dataSource.setUser(dbConnectionConfig.username);
		dataSource.setPassword(dbConnectionConfig.password);
		dataSource.setMaxIdleTime(dbConnectionConfig.maxIdleTime); // set max idle time to 1 hour

		DataAdapter da = new DataAdapter(dataSource);

		return da;
	}

	private void setConfigFileDir(String dir) {
		this.configFileDir = dir;
	}

	public ImageRepository getImageRepository() {
		return new ImageRepository(getDataDirectory()+"pics/", 100, 25);
	}
	
	public String getDataDirectory() {
		if (dataRelativeToConfig) {
			return configFileDir+dataDirectory;
		} else {
			return dataDirectory;
		}
	}
	
	public String getRawDataDirectory() {
		return dataDirectory;
	}
	
	public boolean isDataRelativeToConfig() {
		return dataRelativeToConfig;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public int getRestPort() {
		return restPort;
	}
	
	public int getConsolePort() {
		return consolePort;
	}
	
	public String getGcmApiKey() {
		return gcmApiKey;
	}
	public DbConnectionConfig getDbConnectionConfig() {
		return this.dbConnectionConfig;
	}
	public String getServerName() {
		return serverName;
	}
	public String getDescription() {
		return description;
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

	public ServerInfo getServerInfo() {
		return new ServerInfo(serverName, description, APIS, build);
	}

	@Override
	public String toString() {
		return hostName +":"+ restPort +" (ConsolePort: "+ consolePort +")\n"+ getServerInfo();
	}
}
