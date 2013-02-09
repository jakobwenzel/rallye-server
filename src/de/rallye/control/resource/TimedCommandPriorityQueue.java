/**
 * 
 */
package de.rallye.control.resource;

import java.util.PriorityQueue;

import de.rallye.control.commands.AbstractTimedCommand;

/**
 * @author Felix Huebner
 * @date 13.12.2012
 * 
 */
@SuppressWarnings("serial")
public class TimedCommandPriorityQueue extends
		PriorityQueue<AbstractTimedCommand> {

	/**
	 * this Construktor creates a TimedCommandPriorityQueue. The compare is done
	 * by the TimedCommand.timestamp value. the smallest timestamp will have the
	 * highest priority
	 * 
	 * @param defaultSize
	 *            empty size of the queue after init
	 */
	public TimedCommandPriorityQueue(int defaultSize) {
		super(defaultSize, new TimedCommandComparator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.PriorityQueue#add(java.lang.Object)
	 */
	@Override
	public boolean add(AbstractTimedCommand e) {
		if (e == null) {
			return false;
		} else {
			return super.add(e);
		}
	}

	/**
	 * this method compares all items of the list with the given task
	 * @param type type to compare with
	 * @return true if this type of command is available in the list
	 */
	public boolean containsType(AbstractTimedCommand.Task type) {
		for (AbstractTimedCommand a : this) {
			if (a.getCommand().compareTo(type) == 0)
			return true;
		}
		return false;
	}

}
