/**
 * 
 */
package de.rallye.resource;

import java.util.PriorityQueue;
import de.rallye.resource.TimedCommand;

/**
 * @author Felix Huebner
 * @date 13.12.2012
 *
 */
@SuppressWarnings("serial")
public class TimedCommandPriorityQueue extends PriorityQueue<TimedCommand> {

	/**
	 * this Construktor creates a TimedCommandPriorityQueue. The compare is done by the 
	 * TimedCommand.timestamp value. the smallest timestamp will have the highest priority
	 * @param defaultSize empty size of the queue after init
	 */
	public TimedCommandPriorityQueue(int defaultSize) {
		super(defaultSize, new TimedCommandComparator());
	}
}
