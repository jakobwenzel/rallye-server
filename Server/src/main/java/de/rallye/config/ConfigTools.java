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

	private static final Logger logger = LogManager.getLogger(ConfigTools.class);

	public static String getProjectDir() {
		logger.entry();
		//If we are not runnig from a jar, we are in the classes subdir
		String location = getClassesDir();
		logger.trace("location is "+location);
		//We are either in build/classes/main/
		if (location.endsWith("build/classes/main/")) {
			location = location.substring(0,location.length()-19);	
		//Or in build/libs subdirectory		
		} else if (location.endsWith("build/libs/")) {
			location = location.substring(0,location.length()-11);
		//Or in bin/
		} else if (location.endsWith("bin/")) {
			location = location.substring(0,location.length()-4);
		//If not, we are not running in project dir
		} else {
			logger.warn("not in any subdir known.");
			return null;
		}

		//Remove Server subdir
		location = location.substring(0, location.length()-7);
		
		//There should be a git config directory around here
		try {
			File git = new File(new URL(location+".git").toURI());
			if (!git.isDirectory()) {
				logger.debug("git not found");
				return null;
			}
		} catch (Exception e) {
			logger.catching(e);
			return null;
		}
		//We are now sure this is the correct location
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
	 *  homedir/.rallyeserv-config.json
	 *
	 *  project dir is the directory containing .git. If the .git subdir does not exist,
	 *  the project dir will not be checked for a config.
	 * @return the first Config found
	 */
	public static File findConfigFile() {

		//Try current dir
		File config = new File("config.json");
		logger.trace("Checking locally for '{}'", config);
		if (config.exists())
			return config;

		//Try next to jar/classes
		try {
			config = new File(new URL(ConfigTools.getClassesDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.trace("Checking in jar dir for '{}'", config);
		if (config!=null && config.exists())
			return config;

		//Try project dir
		try {
			config = new File(new URL(ConfigTools.getProjectDir()+"config.json").toURI());
		} catch (Exception e) {
			logger.error(e);
			config = null;
		}
		logger.trace("Checking project dir for '{}'", config);
		if (config!=null && config.exists())
			return config;

		//Try homedir
		String homedir = java.lang.System.getProperty("user.home");
		config = new File(homedir+"/.rallyeserv-config.json");
		logger.trace("Checking home dir for '{}'", config);
		if (config.exists())
			return config;

		//Not found.
		return null;
	}
}
