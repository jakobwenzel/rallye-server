/**
 * 
 */
package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.control.GameControl;
import de.rallye.control.commands.AbstractTimedCommand.Task;
import de.rallye.resource.DataHandler;

/**
 * @author Felix Huebner
 * @date 20.12.2012
 * 
 */
public class NextRound extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(NextRound.class.getName());

	/**
	 * @param control
	 *            GameControl object reference
	 * @param timestamp
	 *            time this command should be executed
	 */
	public NextRound(GameControl control, long timestamp) {
		super(control, control.getDataHandler(), timestamp,
				AbstractTimedCommand.Task.NEXT_ROUND);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		super.data.startNextRound(super.getTimestamp());
		super.data.getGcconfig().setNextRound();
		return logger.exit(NextRound.createNewTask(super.control,
				super.getTimestamp()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.control.commands.AbstractTimedCommand#updateTask()
	 */
	@Override
	public AbstractTimedCommand updateTask(long currentTime) {
		//check if this node is needed anymore
		if (NextRound.getRoundsToPlay(super.control, currentTime) > 0) {
			
			//update the timestamp
			this.setTimestamp(NextRound.getNextExecutionTime(super.control,
					currentTime));
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
		return AbstractTimedCommand.Task.NEXT_ROUND;
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
	public static NextRound createNewTask(GameControl control, long currentTime) {
		// check if we have to play a round, or if all rounds are already played
		int roundsToPlay = 0;
		roundsToPlay = NextRound.getRoundsToPlay(control, currentTime);

		// return the object or null
		if (roundsToPlay > 0) {
			return new NextRound(control, NextRound.getNextExecutionTime(
					control, currentTime));
		} else {
			return null;
		}
	}

	/**
	 * this method calculates the rounds we will have to play
	 * 
	 * @param control
	 *            instance of GameControl
	 * @param currentTime
	 *            time of execution
	 * @return rounds a int
	 */
	private static int getRoundsToPlay(GameControl control, long currentTime) {
		int roundsToPlay = 0;
		if (control.getDataHandler().getGcconfig().getConf_gameStartTime() >= currentTime) {
			roundsToPlay = control.getDataHandler().getGcconfig()
					.getConf_rounds();
		} else {
			roundsToPlay = control.getDataHandler().getGcconfig()
					.getConf_rounds()
					- control.getDataHandler().getGcconfig()
							.getConf_currentRound();
		}
		return roundsToPlay;
	}

	/**
	 * this method calculates the next ExecutionTime of this Task
	 * 
	 * @param control
	 *            instance of GameControl
	 * @param currentTime
	 *            time of execution
	 * @return the next execution time
	 */
	private static long getNextExecutionTime(GameControl control,
			long currentTime) {
		long nextExec = 0;
		if (control.getDataHandler().getGcconfig().getConf_gameStartTime() >= currentTime) {
			nextExec = control.getDataHandler().getGcconfig()
					.getConf_gameStartTime()
					+ control.getDataHandler().getGcconfig()
							.getConf_roundTime();
		} else {
			nextExec = control.getDataHandler().getGcconfig()
					.getConf_roundTime()
					* control.getDataHandler().getGcconfig()
							.getConf_currentRound()
					+ control.getDataHandler().getGcconfig()
							.getConf_gameStartTime();
		}
		return nextExec;
	}
}
