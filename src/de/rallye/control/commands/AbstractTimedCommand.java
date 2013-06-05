package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import de.rallye.control.GameControl;
import de.rallye.resources.DataHandler;
import de.rallye.resources.GameControlConfig;

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
	protected GameControl control = null;

	/**
	 * 
	 * @param timestamp
	 *            time this command should be executed
	 * @param c
	 *            task type
	 * @param data
	 *            DataHandler object reference
	 */
	public AbstractTimedCommand(GameControl control, DataHandler data, long timestamp,Task c) {
		this.data = data;
		this.timestamp = timestamp;
		this.command = c;
		this.control = control;
	}
	
	/**
	 * this method will execute the command, this can only be done once
	 * 
	 * @return if a new command of this type has also to be executed, this
	 *         method returns a new object of this command, otherwise null
	 */
	public abstract AbstractTimedCommand execute();

	/**
	 * this method updates the task to eventually new execution time.
	 * @currentTime timestamp of the update
	 * @return this object or null if this object is not needed anymore.
	 */
	public abstract AbstractTimedCommand updateTask(long currentTime);
	
	/**
	 * @return the timestamp in seconds
	 * @category getter
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 * @category setter
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	} 

	/**
	 * @return the command
	 * @category getter
	 */
	public Task getCommand() {
		return command;
	}
}