package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import de.rallye.resource.DataHandler;
import de.rallye.resource.GameControlConfig;

/**
 * @author Felix Huebner
 * @date 13.12.2012
 * 
 */
public abstract class AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(AbstractTimedCommand.class
			.getName());
	public enum Task {
		GET_STATUS, GAME_START, GAME_END, REMEMBER_NEXT_ROUND, REMEMBER_NEXT_TURN, NEXT_ROUND, UPDATE_VALUES
	};

	private long timestamp = 0;
	private Task command = null;
	private boolean done = false;
	protected DataHandler data = null;

	/**
	 * 
	 * @param timestamp
	 *            time this command should be executed
	 * @param c
	 *            task type
	 * @param data
	 *            DataHandler object reference
	 */
	public AbstractTimedCommand(DataHandler data, long timestamp,Task c) {
		this.data = data;
		this.timestamp = timestamp;
		this.command = c;
	}
	
	/**
	 * this method will execute the command, this can only be done once
	 * 
	 * @return if a new command of this type has also to be executed, this
	 *         method returns a new object of this command, otherwise null
	 */
	public abstract AbstractTimedCommand execute();

	/**
	 * @return the timestamp in seconds
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the command
	 */
	public Task getCommand() {
		return command;
	}
}