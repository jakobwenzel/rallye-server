package de.rallye.exceptions;

public class NodeNotFoundException extends WebAppExcept{

	public NodeNotFoundException() {
		super("Node not found", 404);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 770278077138594737L;

}
