package de.rallye.exceptions;

public class EdgeNotFoundException extends WebAppExcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7178158904648340633L;

	
	public EdgeNotFoundException() {
		super(404,"No linking edge found.");
	}
}
