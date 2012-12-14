/**
 * 
 */
package de.rallye.control;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.rallye.resource.DataHandler;
import de.rallye.resource.TimedCommand;
import de.rallye.resource.TimedCommandPriorityQueue;

/**
 * this class controlls the game and sends push-notifications to the clients
 * 
 * @author Felix Huebner
 * @date 13.12.2012
 * 
 */
public class GameControl extends Thread {

	public static final int TASK_EMPTY = 0;
	public static final int TASK_GETSTATUS = 1;
	public static final int TASK_GAME_START = 2;
	public static final int TASK_GAME_END = 3;
	public static final int TASK_REMEMBER_NEXT_ROUND = 4;
	public static final int TASK_REMEMBER_NEXT_TURN = 5;
	public static final int TASK_NEXT_ROUND = 6;
	public static final int TASK_UPDATE_VALUES = 7;

	private Logger logger = LogManager.getLogger(GameControl.class.getName());
	private DataHandler data = null;
	private boolean stop = false;
	private TimedCommandPriorityQueue tasks = null;
	private TimedCommand next = null;
	private boolean validValues = false;
	private int conf_rounds = 0;
	private int conf_gameStartTime = Integer.MAX_VALUE;
	private int conf_roundTime = Integer.MAX_VALUE;
	private int conf_value_update_time = 10 * 60; // time in seconds

	/**
	 * Constructor
	 * 
	 * @param d
	 *            instance of a DataHandler
	 */
	public GameControl(DataHandler d) {
		logger.entry();
		this.data = d;
		this.loadConfigValues();
		logger.exit();
	}

	/**
	 * this method control the game
	 */
	public void run() {
		logger.entry();

		// init

		// check values JSON
		if (this.validValues == false) {
			logger.fatal("Readout from Database was not successful. this thread is now stopped.");
			return;
		}
		
		this.tasks = new TimedCommandPriorityQueue(5);
		
		long initTime = System.currentTimeMillis() / 1000;

		//// fill the queue,
		
		// set the first status command
		this.tasks.add(new TimedCommand(initTime
				+ this.data.getTimePrintStatus(), GameControl.TASK_GETSTATUS));

		// set the first value update command
		this.tasks.add(new TimedCommand(initTime
				+ this.conf_value_update_time, GameControl.TASK_UPDATE_VALUES));

		// set game start command
		this.tasks.add(new TimedCommand(this.conf_gameStartTime,
				GameControl.TASK_GAME_START));

		
		
		//TODO: add other tasks
		
		
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
			this.processTask(next);

			// get next task from queue
			if (this.tasks.peek() != null) {
				this.next = this.tasks.poll();
			} else {
				this.next = new TimedCommand(
						(System.currentTimeMillis() / 1000) + 10,
						GameControl.TASK_EMPTY);
			}
		}
		// stop/exit stuff if needed
		this.tasks.clear();

		logger.exit();
	}

	/**
	 * this method process the different command types
	 * 
	 * @param task
	 *            command to process
	 */
	private void processTask(TimedCommand task) {
		if (task == null) {
			return;
		}
		// store current time for creation of new tasks if needed
		long currentTime = System.currentTimeMillis() / 1000;

		// decide which task it is and jump to the corresponding method
		switch (task.getCommand()) {
		case GameControl.TASK_EMPTY: {
			logger.entry("TASK_EMPTY");
			if (this.tasks.peek() == null) {
				this.next = new TimedCommand(
						(System.currentTimeMillis() / 1000) + 10,
						GameControl.TASK_EMPTY);
			}
		}
		case GameControl.TASK_GETSTATUS: {
			logger.entry("TASK_GETSTATUS");
			// process printout
			this.printModelStatus();

			// create new task
			this.tasks.add(new TimedCommand(currentTime
					+ this.data.getTimePrintStatus(),
					GameControl.TASK_GETSTATUS));
			break;
		}
		case GameControl.TASK_GAME_START: {
			//TODO: implement
			break;
		}
		case GameControl.TASK_GAME_END: {
			//TODO: implement
			break;
		}
		case GameControl.TASK_NEXT_ROUND: {
			//TODO: implement
			break;
		}
		case GameControl.TASK_REMEMBER_NEXT_ROUND: {
			//TODO: implement
			break;
		}
		case GameControl.TASK_REMEMBER_NEXT_TURN: {
			//TODO: implement
			break;
		}
		case GameControl.TASK_UPDATE_VALUES: {
			logger.entry("TASK_UPDATE_VALUES");

			// process update
			this.loadConfigValues();

			// create new task
			this.tasks.add(new TimedCommand(currentTime
					+ this.conf_value_update_time,
					GameControl.TASK_UPDATE_VALUES));
			break;
		}
		default: {
			break;
		}
		}

		logger.exit();

	}

	/**
	 * print the Status of the Model to log
	 */
	public void printModelStatus() {
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
	 * this method calls a Thread.sleep() for the given ms, if ms negative or 0, no wait is done
	 * 
	 * @param ms
	 *            time to sleep
	 */
	private void wait_ms(long ms) {
		if (ms > 0) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
		
	}

	/**
	 * 
	 */
	private void loadConfigValues() {
		JSONObject o = this.data.getControlData();

		try {
			this.conf_gameStartTime = o.getInt("gameStartTime");
			this.conf_rounds = o.getInt("rounds");
			this.conf_roundTime = o.getInt("roundTime");

			this.validValues = true;
		} catch (JSONException e) {
			logger.catching(e);
			this.validValues = false;
		}
	}

	/**
	 * will init a stop of the thread
	 */
	public void done() {
		logger.entry();
		this.stop = true;
		logger.exit();
	}
}
