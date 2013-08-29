package de.rallye;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.GameState;
import de.rallye.push.PushService;

/**
 * Singleton
 * Initialize with {@link #init()} first
 */
public class RallyeResources {

	private static Logger logger =  LogManager.getLogger(RallyeResources.class);

	private static RallyeResources resources;


	public final DataAdapter data;
	public final ImageRepository imgRepo;
	public final Map<String, ChatPictureLink> hashMap = Collections.synchronizedMap(new HashMap<String, ChatPictureLink>());
	public final PushService push;
	public final RallyeConfig config;
	public GameState gameState;
	
	private RallyeResources() {
		logger.info("Setting up Resources");

		config = loadConfig();

		DataAdapter data;
		try {
			data = config.getMySQLDataAdapter();
		} catch (SQLException e) {
			logger.error("Failed to establish DB connection", e);
			data = null;
		}
		this.data = data;

		imgRepo = config.getImageRepository();

		gameState = new GameState(data);

		push = new PushService(this);
	}

	public GameState getGameState() {
		return gameState;
	}

	/**
	 * Set Up Singleton Pattern
	 */
	public static void init() {
		if (resources!=null) return; //We only want to init once
		resources = new RallyeResources();
	}

	
	/**
	 * Read Config from Config File if present
	 * @return Found Config File or Default Config
	 */
	private RallyeConfig loadConfig() {
		File configFile = findConfigFile();
		if (configFile==null) {
			logger.info("No config file present.");
			return new RallyeConfig();
		}
		logger.info("Loading config file from {}/{}", configFile.getParent(), configFile);
		ObjectMapper mapper = new ObjectMapper();
		try {
			RallyeConfig config = mapper.readValue(configFile, RallyeConfig.class);
			config.setConfigFileDir(configFile.getParent()+File.separator);
			return config;
		} catch ( IOException e) {
			logger.error("Falling back to default config", e);
			return new RallyeConfig();
		}
	}

	/**
	 * Locate a config file.
	 * Following locations are checked in order:
	 *  working dir/config.json
	 *  jar dir/config.json
	 *  project dir/config.json
	 *  homedir/rallyeserv-config.json
	 *  
	 *  project dir is the directory containing .git. If the .git subdir does not exist,
	 *  the project dir will not be checked for a config.
	 * @return the first Config found
	 */
	private File findConfigFile() {
		logger.info("locating config file");
		
		//Try current dir
		File config = new File("./config.json");
		logger.info("Checking for "+config);
		if (config.exists())
			return config;
		
		//Try next to jar/classes
		try {
			config = new File(new URL(getClassesDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.info("Checking for "+config);
		if (config!=null && config.exists())
			return config;
		
		//Try project dir
		try {
			config = new File(new URL(getProjectDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.info("Checking for "+config);
		if (config!=null && config.exists())
			return config;
		
		//Try homedir
		String homedir = System.getProperty("user.home");
		config = new File(homedir+"/.rallyeserv-config.json");
		logger.info("Checking for "+config);
		//logger.info("Homedir location:)
		if (config.exists())
			return config;

		//Not found.
		return null;
	}

	private String getClassesDir() {
		String location = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		//Are we running from inside a jar?
		if (location.endsWith(".jar")) {
			//Strip jar filename
			location = location.substring(0,location.lastIndexOf('/')+1);
		}
		if (!location.endsWith("/"))
			location = location+"/"; //Add a slash to end if missing
		return location;
	}
	
	private String getProjectDir() {
		//If we are not runnig from a jar, we are in the classes subdir
		String location = getClassesDir();
		if (location.endsWith("classes/")) {
			location = location.substring(0,location.length()-8);
		}
		//We should be in target subdirectory
		if (!location.endsWith("target/"))
			//If not, we are not running in project dir
			return null;
		//remove target
		location = location.substring(0,location.length()-7);
		//There should be a git config directory around here
		try {
			File git = new File(new URL(location+".git").toURI());
			if (!git.isDirectory()) 
				return null;
		} catch (Exception e) {
			return null;
		}
		//We are sure this is the correct location
		return location;
	}

	public RallyeConfig getConfig() {
		return config;
	}
	
	public static RallyeResources getResources() {
		if (resources == null)
			throw new IllegalStateException("Call init() first");
		return resources;
	}
}
