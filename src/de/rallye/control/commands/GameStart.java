/**
 * 
 */
package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.control.GameControl;
import de.rallye.control.commands.AbstractTimedCommand.Task;
import de.rallye.resources.DataHandler;

/**
 * @author Felix Huebner
 * @date 19.12.2012
 * 
 */
public class GameStart extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(GameStart.class.getName());

	/**
	 * @param control
	 *            GameControl object reference
	 * @param timestamp
	 *            time this command should be executed
	 */
	public GameStart(GameControl control, long timestamp) {
		super(control, control.getDataHandler(), timestamp,
				AbstractTimedCommand.Task.GAME_START);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		this.data.startGame(super.getTimestamp());
		this.data.getGcconfig().setNextRound();
		return logger.exit(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.control.commands.AbstractTimedCommand#updateTask()
	 */
	@Override
	public AbstractTimedCommand updateTask(long currentTime) {
		if (control.getDataHandler().getGcconfig().getConf_gameStartTime() >= currentTime) {
			return this;
		} else {
			return null;
		}
	}
	
	/**
	 * this method returns the Type of this command.
	 * @return the type of this command
	 */
	public static Task getCommandType() {
		return AbstractTimedCommand.Task.GAME_START;
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
	public static GameStart createNewTask(GameControl control, long currentTime) {
		if (control.getDataHandler().getGcconfig().getConf_gameStartTime() >= currentTime) {
			return new GameStart(control, control.getDataHandler()
					.getGcconfig().getConf_gameStartTime());
		} else {
			return null;
		}
	}
}
