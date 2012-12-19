package de.rallye.control.resource;

import java.util.Comparator;

import de.rallye.control.commands.AbstractTimedCommand;

/**
 * this class represents a comparator.
 * it will return -1 if arg0 is less than arg1
 * it will return 0 if both timestamps are equal
 * it will return 1 if arg0 is greater than arg1
 * 
 * @author Felix Huebner
 * @date 13.12.2012
 *
 */
public class TimedCommandComparator implements Comparator<AbstractTimedCommand> {

	@Override
	public int compare(AbstractTimedCommand arg0, AbstractTimedCommand arg1) {
		
		if (arg0.getTimestamp() < arg1.getTimestamp()) {
			return -1;
		}
		if (arg0.getTimestamp() > arg1.getTimestamp()) {
			return 1;
		}
		return 0;
	}
	
}