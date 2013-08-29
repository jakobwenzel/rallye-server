package de.rallye;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

import de.rallye.images.ImageRepository;
import de.rallye.model.structures.LatLng;
import de.rallye.model.structures.ServerInfo;

/**
 * This class has setters for all configurable settings, to be used for settings loading
 * @author Jakob Wenzel
 *
 */
public class RallyeConfigLoad extends RallyeConfig {
	private static final Logger logger = LogManager.getLogger(RallyeConfigLoad.class);
	
	public void setHostName(String host) {
		this.host = host;
	}
	
	public void setRestPort(int port) {
		this.port = port;
	}
	
	public void setConsolePort(int port) {
		this.consolePort = port;
	}
	
	public void setGcmKey(String key) {
		this.gcmApiKey = key;
	}

	public void setMapBounds(List<LatLng> bounds) {
		this.mapBounds = bounds.toArray(mapBounds);
	}
	
	public void setMapCenter(LatLng center) {
		this.mapCenter = center;
	}
	

	public String getServerName() {
		return name;
	}
	public void setServerName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public DbData getDbConnection() {
		return this.dbData;
	}
	public void setDbConnection(DbData data) {
		this.dbData = data;
	}
	public void setDataDirectory(String dir) {
		this.dataDirectory = dir;
	}
	public void setDataRelative(Boolean relative) {
		logger.info("setting datarelative "+relative);
		this.dataRelative = relative;
	}
	
}
