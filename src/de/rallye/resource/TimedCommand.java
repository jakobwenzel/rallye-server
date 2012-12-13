/**
 * 
 */
package de.rallye.resource;

/**
 * @author Felix Huebner
 * @date 13.12.2012
 *
 */
public class TimedCommand {
	private long timestamp = 0;
	private int command = 0;
	
	/**
	 * 
	 * @param t
	 * @param c
	 */
	public TimedCommand(long t, int c) {
		this.timestamp = t;
		this.command = c;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the command
	 */
	public int getCommand() {
		return command;
	}
	
	
}