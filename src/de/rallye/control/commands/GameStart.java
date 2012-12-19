/**
 * 
 */
package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.resource.DataHandler;

/**
 * @author Felix Huebner
 * @date 19.12.2012
 *
 */
public class GameStart extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(GameStart.class.getName());
	/**
	 * @param data  DataHandler object reference
	 * @param timestamp time this command should be executed
	 */
	public GameStart(DataHandler data, long timestamp) {
		super(data, timestamp, AbstractTimedCommand.Task.GAME_START);
	}

	/* (non-Javadoc)
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		this.data.startGame(super.getTimestamp());
		return logger.exit(null);
	}

	/**
	 * this method returns a new Object of this Task
	 * 
	 * @param data
	 *            reference of the DataHandler object
	 * @param currentTime
	 *            time when this method is called
	 * @return this method returns a new Object of this Task if one is needed,
	 *         if not this method will return null
	 */
	public static GameStart createNewTask(DataHandler data, long currentTime) {
		if (data.getGcconfig().getConf_gameStartTime() >= currentTime) {
			return new GameStart(data, data.getGcconfig().getConf_gameStartTime());
		} else {
			return null;
		}
	}
}
