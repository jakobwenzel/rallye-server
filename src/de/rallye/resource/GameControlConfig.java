package de.rallye.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class GameControlConfig {
	private Logger logger = LogManager.getLogger(GameControlConfig.class
			.getName());	
	private boolean conf_validValues = false;
	private int conf_rounds = 0;
	private int conf_gameStartTime = Integer.MAX_VALUE;
	private int conf_roundTime = Integer.MAX_VALUE;
	private int conf_update_time = Integer.MAX_VALUE;//10 * 60; // time in seconds //TODO: this has to move to dataHandler
	private int conf_printStatus = Integer.MAX_VALUE;//5*60; // time in seconds //TODO: this has to move to dataHandler
	private DataHandler data = null;

	/**
	 * Constructor
	 * @param d a valid DataHandler object
	 */
	public GameControlConfig(DataHandler d) {
		this.data = d;
	}

	/**
	 * try to update the config values from database if an error occure in this
	 * step the variables will be set to default values
	 */
	public void loadConfigValues() {
		logger.entry();
		JSONObject o = this.data.getControlData();
		if (o == null) {
			this.setConfigToDefaults();
			return;
		}

		try {
			this.conf_gameStartTime = o.getInt("gameStartTime");
			this.conf_rounds = o.getInt("rounds");
			this.conf_roundTime = o.getInt("roundTime");
			
			//TODO load from dataHandler
			
			this.conf_update_time = 10*60;
			this.conf_printStatus = 5*60;
			
			this.conf_validValues = true;
			logger.info("Update successful.");
		} catch (JSONException e) {
			logger.catching(e);
			this.setConfigToDefaults();
		}
	}

	/**
	 * set config to default settings
	 */
	private void setConfigToDefaults() {
		this.conf_rounds = 0;
		this.conf_gameStartTime = Integer.MAX_VALUE;
		this.conf_roundTime = Integer.MAX_VALUE;
		this.conf_update_time = Integer.MAX_VALUE;
		this.conf_printStatus = Integer.MAX_VALUE;
		
		this.conf_validValues = false;
	}

	/**
	 * @return the conf_validValues
	 * @category getter
	 */
	public boolean isConfValid() {
		return conf_validValues;
	}

	/**
	 * @return the conf_rounds
	 * @category getter
	 */
	public int getConf_rounds() {
		return conf_rounds;
	}

	/**
	 * @return the conf_gameStartTime
	 * @category getter
	 */
	public int getConf_gameStartTime() {
		return conf_gameStartTime;
	}

	/**
	 * @return the conf_roundTime
	 * @category getter
	 */
	public int getConf_roundTime() {
		return conf_roundTime;
	}

	/**
	 * @return the conf_value_update_time
	 * @category getter
	 */
	public int getConf_value_update_time() {
		return conf_update_time;
	}

	/**
	 * 
	 * @return the conf getStatus update time
	 * @category getter
	 */
	public int getConf_getStatus_update_time() {
		return this.conf_printStatus;
	}

}