/**
 * 
 */
package de.rallye.control.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.control.GameControl;

/**
 * @author Felix Huebner
 * @date 19.12.2012
 * 
 */
public class GetStatus extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(GetStatus.class.getName());

	/**
	 * @param control GameControl object reference
	 * @param timestamp time this command should be executed
	 */
	public GetStatus(GameControl control, long timestamp) {
		super(control, control.getDataHandler(), timestamp, AbstractTimedCommand.Task.GET_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		String s = super.data.getModelStatus();
		for (String str : s.split("\n")) {
			logger.info(str);
		}
		for (String str : super.control.getStatus().split("\n")) {
			logger.info("GameControl: "+str);
		}
		//return a new GetStatus Task
		return logger.exit(GetStatus.createNewTask(super.control, super.getTimestamp()));
	}
	
	/* (non-Javadoc)
	 * @see de.rallye.control.commands.AbstractTimedCommand#updateTask()
	 */
	@Override
	public AbstractTimedCommand updateTask(long currentTime) {
		return this;
	}
	
	/**
	 * this method returns the Type of this command.
	 * @return the type of this command
	 */
	public static Task getCommandType() {
		return AbstractTimedCommand.Task.GET_STATUS;
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
	public static GetStatus createNewTask(GameControl control, long currentTime) {
		return new GetStatus(control, currentTime
				+ control.getDataHandler().getGcconfig().getConf_getStatus_update_time());
	}

	
}
