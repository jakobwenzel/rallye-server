/**
 * 
 */
package de.rallye.control;

import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.rallye.control.commands.AbstractTimedCommand;
import de.rallye.control.commands.GameStart;
import de.rallye.control.commands.GetStatus;
import de.rallye.control.commands.NextRound;
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
	private long stepTime = 0;

	/**
	 * Constructor
	 * 
	 * @param data
	 *            instance of a DataHandler
	 * @param stepTime
	 * 			delay time of the thread, this time indicates the maximal sleep time of the thread in ms
	 */
	public GameControl(DataHandler data, long stepTime) {
		logger.entry();
		this.data = data;
		this.stepTime = stepTime;
		// this.process = new GameControlProcess(d,DataHandler.gcconfig);
		this.data.getGcconfig().loadConfigValues(); // if this is called before
													// the DataHandler is
													// created a NullPointer
													// exception will be thrown
		logger.exit();
	}

	/**
	 * will init a stop of the thread
	 */
	public void done() {
		logger.entry();
		this.stop = true;
		this.interrupt();
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

		// fill the queue,
		this.fillQueue(initTime, true);

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
			logger.trace("Next Execution: " + next.getCommand().toString()
					+ " " + this.timestampToData(next.getTimestamp())
					+ " (Sleep: "
					+ (next.getTimestamp() * 1000 - System.currentTimeMillis())
					+ "ms)");

			// wait for next task
			this.wait_ms(next.getTimestamp() * 1000
					- System.currentTimeMillis());

			// debug
			// logger.trace("Current Process: "
			// + this.timestampToData(System.currentTimeMillis() / 1000));

			// process the task
			if (!this.stop) {
				this.tasks.add(this.next.execute());

				// get next task from queue
				if (this.tasks.peek() != null) {
					this.next = this.tasks.poll();
				}
			}
		}
		// stop/exit stuff if needed
		this.cleanUp();

		logger.exit();
	}

	/**
	 * this method has to move to DataHandler as Abstract method
	 * 
	 * @param initTime
	 *            time of the fill
	 * @param cleanQueue
	 *            if this is true the queue is completely cleaned before adding
	 *            the entries, if false is set the elements in the queue will
	 *            updated/checked
	 */
	public void fillQueue(long fillTime, boolean cleanQueue) {
		if (cleanQueue) {
			// clean queue and add new items
			this.tasks.clear();

			// set the first getstatus command
			this.tasks.add(GetStatus.createNewTask(this, fillTime));

			// set the first value update command
			this.tasks.add(UpdateValues.createNewTask(this, fillTime));

			// set game start command
			this.tasks.add(GameStart.createNewTask(this, fillTime));

			// set next round command
			this.tasks.add(NextRound.createNewTask(this, fillTime));
		
		} else {
			
			// update/and add tasks
			AbstractTimedCommand n = null;
			LinkedList<AbstractTimedCommand> nlst = new LinkedList<AbstractTimedCommand>();
			for (AbstractTimedCommand a : this.tasks) {
				n = a.updateTask(fillTime);
				if (n != null) {
					nlst.add(n);
					n = null;
				}
			}
			// remove all elements except of the elements in list nlst
			this.tasks.retainAll(nlst);
			
			//check if getStatus exists
			if (!this.tasks.containsType(GetStatus.getCommandType())) {
				this.tasks.add(GetStatus.createNewTask(this, fillTime));
			}
			
			//check if value update exists
			if (!this.tasks.containsType(UpdateValues.getCommandType())) {
				this.tasks.add(UpdateValues.createNewTask(this, fillTime));
			}
			
			//check if start command exists
			if (!this.tasks.containsType(GameStart.getCommandType())) {
				this.tasks.add(GameStart.createNewTask(this, fillTime));
			}
			
			//check if next round exists
			if (!this.tasks.containsType(NextRound.getCommandType())) {
				this.tasks.add(NextRound.createNewTask(this, fillTime));
			}
		}
	}

	/**
	 * print the Status of this Class
	 */
	public String getStatus() {
		StringBuilder str = new StringBuilder();
		int count = this.tasks.size();

		if (next != null) {
			count++;
		}
		str.append("Num Tasks: ").append(count).append("\n");
		if (next != null) {
			str.append("Next Task: Type: ")
					.append(this.next.getCommand().toString())
					.append(" Execution Time: ")
					.append(this.timestampToData(this.next.getTimestamp()))
					.append("\n");
		}
		for (AbstractTimedCommand a : this.tasks) {
			str.append("Next Task: Type: ").append(a.getCommand().toString())
					.append(" Execution Time: ")
					.append(this.timestampToData(a.getTimestamp()))
					.append("\n");
		}
		return str.toString();
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
		long endTime = System.currentTimeMillis()+ms;
		long doubleStepTime = this.stepTime*2;
		
		//check if illigal values are given
		if (ms <= 0) {
			return;
		}
		
		logger.trace("complete sleep: "+ms);
		
		//loop
		try {
		while (ms > 0) {
			ms = endTime-System.currentTimeMillis();
			if (ms > doubleStepTime) {
				// sleep the maximal sleep time
				GameControl.sleep(this.stepTime);
			} else if (ms > this.stepTime) {
				// between double stepTime and and single stepTime
				logger.trace("second last sleep: "+(ms/2));
				GameControl.sleep(ms/2);
				
			} else  {
				// sleep the rest waitTime
				logger.trace("last sleep: "+ms);
				GameControl.sleep(ms);
				ms = 0;
			}
			
			
			// check if a GameControl.done was called. if this was called we have to leave
			if (this.stop) {
				ms = 0;
			}
		}
		} catch (InterruptedException e) {
			logger.catching(e);
		}
	}

	/**
	 * @return the DataHandler object
	 */
	public DataHandler getDataHandler() {
		return this.data;
	}
}