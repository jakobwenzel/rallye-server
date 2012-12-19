/**
 * 
 */
package de.rallye.control;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.rallye.control.commands.AbstractTimedCommand;
import de.rallye.control.commands.GameStart;
import de.rallye.control.commands.GetStatus;
import de.rallye.control.commands.UpdateValues;
import de.rallye.control.resource.TimedCommandPriorityQueue;
import de.rallye.resource.DataHandler;
import de.rallye.resource.GameControlConfig;

/**
 * this class controls the game and sends push-notifications to the clients
 * 
 * @author Felix Huebner
 * @date 13.12.2012
 * 
 */
public class GameControl extends Thread {

	private Logger logger = LogManager.getLogger(GameControl.class.getName());
	private DataHandler data = null;
	private boolean stop = false;
	private TimedCommandPriorityQueue tasks = null;
	private AbstractTimedCommand next = null;
	private GameControlProcess process = null;

	/**
	 * Constructor
	 * 
	 * @param d
	 *            instance of a DataHandler
	 */
	public GameControl(DataHandler d) {
		logger.entry();
		this.data = d;
//		this.process = new GameControlProcess(d,DataHandler.gcconfig);
		this.data.getGcconfig().loadConfigValues(); // if this is called before the DataHandler is created a NullPointer exception will be thrown
		logger.exit();
	}

	/**
	 * will init a stop of the thread
	 */
	public void done() {
		logger.entry();
		this.stop = true;
		this.notify();
		logger.exit();
	}
	
	/**
	 * call this method before leaving the thread
	 */
	private void cleanUp() {
		this.tasks.clear();
	}

	/**
	 * this method control the game
	 */
	public void run() {
		logger.entry();
		// init
		
		// check values JSON
		if (!this.data.getGcconfig().isConfValid()) {
			logger.fatal("Readout from Database was not successful. this thread is now stopped.");
			this.cleanUp();
			return;
		}

		this.tasks = new TimedCommandPriorityQueue(5);

		long initTime = System.currentTimeMillis() / 1000;

		// // fill the queue,

		// set the first getstatus command
		this.tasks.add(GetStatus.createNewTask(data, initTime));

		// set the first value update command
		this.tasks.add(UpdateValues.createNewTask(data, initTime));

		// set game start command
		this.tasks.add(GameStart.createNewTask(data, initTime));
		
		
		// TODO: add other tasks

		// extract the first element from queue and store it in this.next
		this.next = this.tasks.poll();

		while (!this.stop) {
			// check of the queue runs empty
			if (next == null) {
				logger.fatal("logger runs out of processes. Stop Task.");
				this.done();
			}

			// debug
			logger.trace("Next Execution:  "
					+ this.timestampToData(next.getTimestamp()) + " (Sleep: "
					+ (next.getTimestamp() * 1000 - System.currentTimeMillis())
					+ "ms)");

			// wait for next task
			this.wait_ms(next.getTimestamp() * 1000
					- System.currentTimeMillis());

			// debug
			logger.trace("Current Process: "
					+ this.timestampToData(System.currentTimeMillis() / 1000));

			// process the task
				this.tasks.add(this.next.execute());

			//this.processTask(next);

			// get next task from queue
			if (this.tasks.peek() != null) {
				this.next = this.tasks.poll();
			} else {
				//TODO this.next = new AbstractTimedCommand(
					//	(System.currentTimeMillis() / 1000) + 10,
					//	AbstractTimedCommand.TASK_EMPTY);
			}
		}
		// stop/exit stuff if needed
		this.cleanUp();

		logger.exit();
	}

	/**
	 * this method process the different command types
	 * 
	 * @param task
	 *            command to process
	 */
	/*private void processTask(AbstractTimedCommand task) {
		if (task == null) {
			return;
		}
		// store current time for creation of new tasks if needed
		long currentTime = System.currentTimeMillis() / 1000;

		// decide which task it is and jump to the corresponding method
		switch (task.getCommand()) {
		case AbstractTimedCommand.TASK_EMPTY: {
			logger.entry("TASK_EMPTY");
			if (this.tasks.peek() == null) {
				this.next = new AbstractTimedCommand(
						(System.currentTimeMillis() / 1000) + 10,
						AbstractTimedCommand.TASK_EMPTY);
			}
		}
		case AbstractTimedCommand.TASK_GETSTATUS: {
			logger.entry("TASK_GETSTATUS");
			// process printout
			this.printModelStatus();

			// create new task
			this.tasks.add(new AbstractTimedCommand(currentTime
					+ DataHandler.gcconfig.getConf_getStatus_update_time(),
					AbstractTimedCommand.TASK_GETSTATUS));
			break;
		}
		case AbstractTimedCommand.TASK_GAME_START: {
			//process the command
			AbstractTimedCommand t = this.process.taskGameStart(currentTime);
			// and add a new task to taskpool if needed
			if (t != null) {
				this.tasks.add(t);
			}
			break;
		}
		case AbstractTimedCommand.TASK_GAME_END: {
			// TODO: implement
			break;
		}
		case AbstractTimedCommand.TASK_NEXT_ROUND: {
			// TODO: implement
			break;
		}
		case AbstractTimedCommand.TASK_REMEMBER_NEXT_ROUND: {
			// TODO: implement
			break;
		}
		case AbstractTimedCommand.TASK_REMEMBER_NEXT_TURN: {
			// TODO: implement
			break;
		}
		case AbstractTimedCommand.TASK_UPDATE_VALUES: {
			logger.entry("TASK_UPDATE_VALUES");

			// process update
			DataHandler.gcconfig.loadConfigValues();

			// create new task
			this.tasks.add(new AbstractTimedCommand(currentTime
					+ DataHandler.gcconfig.getConf_value_update_time(),
					AbstractTimedCommand.TASK_UPDATE_VALUES));
			break;
		}
		default: {
			break;
		}
		}

		logger.exit();

	}*/

	/**
	 * print the Status of the Model to log
	 */
	public void printModelStatus() {
		// process printout
		String s = this.data.getModelStatus();
		for (String str : s.split("\n")) {
			logger.info(str);
		}
		logger.info("GameControl: numTasks: " + this.tasks.size());
	}

	/**
	 * return a string with a readable date from a timestamp
	 * 
	 * @param timestamp
	 *            to change to a readable string
	 * @return string of the timestamp
	 */
	public String timestampToData(long timestamp) {
		Date time = new Date(timestamp * 1000);
		return time.toString();
	}

	/**
	 * this method calls a Thread.sleep() for the given ms, if ms negative or 0,
	 * no wait is done
	 * 
	 * @param ms
	 *            time to sleep
	 */
	private void wait_ms(long ms) {
		if (ms > 0) {
			try {
				sleep(ms);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}

	}

}

final class GameControlProcess {
	private Logger logger = LogManager.getLogger(GameControlProcess.class
			.getName());
	private DataHandler data = null;
	private GameControlConfig config = null;

	GameControlProcess(DataHandler d, GameControlConfig c) {
		this.data = d;
		this.config = c;
	}

	/**
	 * process the task: game_start
	 * 
	 * @param time timestamp of the time the method was called
	 * @return null
	 */
	public AbstractTimedCommand taskGameStart(long time) {
		//process the command in model
		this.data.startGame(time);
		return null;
	}
}
