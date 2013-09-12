package de.rallye.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Ramon
 * Date: 31.08.13
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class ConfigTools {

	private static Logger logger = LogManager.getLogger(ConfigTools.class);

	public static String getProjectDir() {
		logger.entry();
		//If we are not runnig from a jar, we are in the classes subdir
		String location = getClassesDir();
		if (location.endsWith("classes/")) {
			location = location.substring(0,location.length()-8);
		}
		//We should be in build/libs subdirectory
		if (!location.endsWith("build/libs/")) {
			//If not, we are not running in project dir
			logger.debug("no build/libs/");
			return null;
		}
		//remove build/libs/
		location = location.substring(0,location.length()-11);
		//There should be a git config directory around here
		try {
			File git = new File(new URL(location+".git").toURI());
			if (!git.isDirectory()) {
				logger.debug("git not found");
				return null;
			}
		} catch (Exception e) {
			logger.debug(e);
			return null;
		}
		//We are sure this is the correct location
		return location;
	}

	public static String getClassesDir() {
		String location = ConfigTools.class.getProtectionDomain().getCodeSource().getLocation().toString();
		//Are we running from inside a jar?
		if (location.endsWith(".jar")) {
			//Strip jar filename
			location = location.substring(0,location.lastIndexOf('/')+1);
		}
		if (!location.endsWith("/"))
			location = location+"/"; //Add a slash to end if missing
		return location;
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
	public static File findConfigFile() {
		logger.info("locating config file");

		//Try current dir
		File config = new File("config.json");
		logger.info("Checking locally for '{}'", config);
		if (config.exists())
			return config;

		//Try next to jar/classes
		try {
			config = new File(new URL(ConfigTools.getClassesDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.info("Checking in jar dir for '{}'", config);
		if (config!=null && config.exists())
			return config;

		//Try project dir
		try {
			config = new File(new URL(ConfigTools.getProjectDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.info("Checking project dir for '{}'", config);
		if (config!=null && config.exists())
			return config;

		//Try homedir
		String homedir = java.lang.System.getProperty("user.home");
		config = new File(homedir+"/.rallyeserv-config.json");
		logger.info("Checking home dir for '{}'", config);
		//logger.info("Homedir location:)
		if (config.exists())
			return config;

		//Not found.
		return null;
	}
}
