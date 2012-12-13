/**
 * 
 */
package de.rallye.control;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public static final int TASK_GETSTATUS = 0;
	public static final int TASK_GAME_START = 1;
	public static final int TASK_GAME_END = 2;
	public static final int TASK_REMEMBER_NEXT_ROUND = 3;
	public static final int TASK_REMEMBER_NEXT_TURN = 4;
	public static final int TASK_NEXT_ROUND = 5;

	private Logger logger = LogManager.getLogger(GameControl.class.getName());
	private DataHandler data = null;
	private boolean stop = false;
	private TimedCommandPriorityQueue tasks = null;
	private TimedCommand next = null;

	/**
	 * Construktor
	 * 
	 * @param d
	 */
	public GameControl(DataHandler d) {
		logger.entry();
		this.data = d;
		logger.exit();
	}

	/**
	 * this method control the game
	 */
	public void run() {
		logger.entry();

		// init
		// TODO: fill queue with gameplay values
		this.tasks = new TimedCommandPriorityQueue(5);
		long initTime = System.currentTimeMillis() / 1000;

		// set the first status command
		this.tasks.add(new TimedCommand(initTime
				+ this.data.getTimePrintStatus(), GameControl.TASK_GETSTATUS));

		// add fill of the queue, get the first element
		this.next = this.tasks.poll();

		while (!this.stop) {
			logger.trace("Next Process: " + this.timestampToData(next.getTimestamp()) + " (Sleep: "
					+ (next.getTimestamp() * 1000 - System.currentTimeMillis())
					+ "ms)");
			try {
				Thread.sleep((next.getTimestamp() * 1000 - (System
						.currentTimeMillis())));
			} catch (InterruptedException e) {
				logger.catching(e);
			}
			logger.trace("Next Process: " + this.timestampToData(System.currentTimeMillis() / 1000));

			this.processTask(next);
			this.next = this.tasks.poll();
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

		//decide which task it is and jump to the corresponding method
		switch (task.getCommand()) {
		case GameControl.TASK_GETSTATUS: {
			// process printout
			this.printModelStatus();
			
			// create new task
			this.tasks.add(new TimedCommand(currentTime
					+ this.data.getTimePrintStatus(),
					GameControl.TASK_GETSTATUS));
			break;
		}
		default: {
			break;
		}
		}

	}

	/**
	 * print the Status of the Model to log
	 */
	public void printModelStatus() {
		String s = this.data.getModelStatus();
		for (String str : s.split("\n")) {
			logger.info(str);
		}
		logger.info("GameControl: numTasks: "+this.tasks.size());

	}
	
	/**
	 * return a string with a readable date from a timestamp
	 * @param timestamp to change to a readable string
	 * @return string of the timestamp
	 */
	public String timestampToData(long timestamp) {
		 Date time = new Date(timestamp*1000);
		
		return time.toString();
		
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
