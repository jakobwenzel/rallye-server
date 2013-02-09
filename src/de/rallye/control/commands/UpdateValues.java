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
 * @date 19.12.2012
 *
 */
public class UpdateValues extends AbstractTimedCommand {
	private Logger logger = LogManager.getLogger(UpdateValues.class.getName());
	/**
	 * @param control GameControl object reference
	 * @param timestamp time this command should be executed
	 */
	public UpdateValues(GameControl control, long timestamp) {
		super(control,control.getDataHandler(), timestamp, AbstractTimedCommand.Task.UPDATE_VALUES);
	}

	/* (non-Javadoc)
	 * @see de.rallye.control.commands.AbstractTimedCommand#execute()
	 */
	@Override
	public AbstractTimedCommand execute() {
		logger.entry();
		this.data.getGcconfig().loadConfigValues();
		this.control.fillQueue(super.getTimestamp(), false);
		// return null here because fillQueue will add an element of UpdateValues
		return logger.exit(null);
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
		return AbstractTimedCommand.Task.UPDATE_VALUES;
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
	public static UpdateValues createNewTask(GameControl control, long currentTime) {
		return new UpdateValues(control, currentTime
				+ control.getDataHandler().getGcconfig().getConf_value_update_time());
	}
}
