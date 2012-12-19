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
public class GetStatus extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(GetStatus.class.getName());

	/**
	 * @param data  DataHandler object reference
	 * @param timestamp time this command should be executed
	 */
	public GetStatus(DataHandler data, long timestamp) {
		super(data, timestamp, AbstractTimedCommand.Task.GET_STATUS);
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

		//return a new GetStatus Task
		return logger.exit(GetStatus.createNewTask(super.data, super.getTimestamp()));
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
	public static GetStatus createNewTask(DataHandler data, long currentTime) {
		return new GetStatus(data, currentTime
				+ data.getGcconfig().getConf_getStatus_update_time());
	}
}
