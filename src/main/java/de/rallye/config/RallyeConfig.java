package de.rallye.config;

import de.rallye.api.Game;
import de.rallye.api.System;
import de.rallye.api.Tasks;
import de.rallye.model.structures.MapConfig;
import de.rallye.model.structures.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@JsonIgnoreProperties(ignoreUnknown = true)
public class RallyeConfig {
	
	private static final Logger logger = LogManager.getLogger(RallyeConfig.class);
	
	//This is the default data.
	@JsonProperty private String hostName = "0.0.0.0";
	@JsonProperty private int restPort = 10101;
	@JsonProperty private int consolePort = 10100;
	@JsonProperty private String gcmApiKey = "";
	@JsonProperty private String serverName = "";
	@JsonProperty private String description = "";
	@JsonProperty private MapConfig mapConfig;
	@JsonProperty private ImageCacheConfig imageCacheConfig = new ImageCacheConfig(100, 25);

	@JsonProperty private String configFileDir = "";

	public static class DbConnectionConfig {
		public String connectString;
		public String username;
		public String password;
		public int maxIdleTime = 3600;
	}

	@JsonProperty private DbConnectionConfig dbConnectionConfig = new DbConnectionConfig();
	@JsonProperty private String dataDirectory = "data/";
	@JsonProperty private boolean dataRelativeToConfig = false;

	//TODO: read Api versions from modules / manifest
	private final ServerInfo.Api[] APIS = {new ServerInfo.Api(Tasks.API_NAME, Tasks.API_VERSION), new ServerInfo.Api(Game.API_NAME, Game.API_VERSION), new ServerInfo.Api(System.API_NAME, System.API_VERSION)};
	private String build;

	/**
	 * Read Config from Config File if present
	 * @return Found Config File or Default Config
	 */
	public static RallyeConfig fromFile(File configFile, GitRepositoryState git) {
		RallyeConfig config;

		if (configFile==null) {
			logger.error("No config file found.");
			return null;
		}
		logger.info("Loading config file from {}/{}", configFile.getParent(), configFile);
		ObjectMapper mapper = new ObjectMapper();
		try {
			config = mapper.readValue(configFile, RallyeConfig.class);
			File parent = configFile.getParentFile();
			if (parent!=null)
				config.configFileDir = parent + File.separator;
			else
				config.configFileDir = "";

			config.setGit(git);
			logger.debug(config.toString());
			return config;
		} catch ( IOException e) {
			logger.error("Config invalid.", e);
			return null;
		}
	}

	public static RallyeConfig fromStream(InputStream stream) {
		if (stream==null) {
			logger.warn("No config stream. Using Default Config");
			return new RallyeConfig();
		}
		logger.info("Loading config file from stream");
		ObjectMapper mapper = new ObjectMapper();
		try {
			RallyeConfig config = mapper.readValue(stream, RallyeConfig.class);
			
			if (config.isDataRelativeToConfig()) {
				logger.warn("Data relative to config is not supported for Stream Config. Falling back to default config");
				return new RallyeConfig();
			}
				
			logger.debug(config.toString());
			return config;
		} catch ( IOException e) {
			logger.error("Falling back to default config", e);
			return new RallyeConfig();
		}
	}
	
	private void setGit(GitRepositoryState git) {
		String build;

		if (git != null) {
			build = git.getDescription()+", "+git.getBuildTime();
		} else {
			build = "No build info";
		}

		this.build = build;
	}

	public String getImageRepositoryPath() {
		return getDataDirectory()+"pics/";
	}
	
	public String getDataDirectory() {
		if (dataRelativeToConfig) {
			return configFileDir+dataDirectory;
		} else {
			return dataDirectory;
		}
	}
	
//	public String getRawDataDirectory() {
//		return dataDirectory;
//	}
	
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

	public ImageCacheConfig getImageCacheConfig() {
		return imageCacheConfig;
	}

	public MapConfig getMapConfig() {
		return mapConfig;
	}

	public ServerInfo getServerInfo() {
		return new ServerInfo(serverName, description, APIS, build);
	}

	@Override
	public String toString() {
		return hostName +":"+ restPort +" (ConsolePort: "+ consolePort +")\n"+ getServerInfo();
	}
}