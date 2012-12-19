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
public class UpdateValues extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(UpdateValues.class.getName());
	/**
	 * @param data  DataHandler object reference
	 * @param timestamp time this command should be executed
	 */
	public UpdateValues(DataHandler data, long timestamp) {
		super(data, timestamp, AbstractTimedCommand.Task.UPDATE_VALUES);
	}

	/* (non-Javadoc)
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		this.data.getGcconfig().loadConfigValues();
		return logger.exit(UpdateValues.createNewTask(super.data, super.getTimestamp()));
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
	public static UpdateValues createNewTask(DataHandler data, long currentTime) {
		return new UpdateValues(data, currentTime
				+ data.getGcconfig().getConf_value_update_time());
	}
}
