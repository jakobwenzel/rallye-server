package de.rallye;

import java.util.ArrayList;
import java.util.List;

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
	
	public void setHostName(String host) {
		this.HOST = host;
	}
	
	public void setRestPort(int port) {
		this.PORT = port;
	}
	
	public void setConsolePort(int port) {
		this.CONSOLE_PORT = port;
	}
	
	public void setGcmKey(String key) {
		this.GCM_API_KEY = key;
	}

	public void setMapBounds(List<LatLng> bounds) {
		this.MAP_BOUNDS = bounds.toArray(MAP_BOUNDS);
	}
	
	public void setMapCenter(LatLng center) {
		this.MAP_CENTER = center;
	}
	

	public String getServerName() {
		return NAME;
	}
	public void setServerName(String name) {
		this.NAME = name;
	}
	
	public String getDescription() {
		return DESCRIPTION;
	}
	public void setDescription(String description) {
		this.DESCRIPTION = description;
	}
	public DbData getDbConnection() {
		return this.DB_DATA;
	}
	public void setDbConnection(DbData data) {
		this.DB_DATA = data;
	}
	
}
