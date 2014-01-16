package de.rallye.exceptions;

public class NodeOccupiedException extends WebAppExcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9195174029827064650L;
	
	public NodeOccupiedException() {
		super("Destination node occupied.", 409);
	}

}
