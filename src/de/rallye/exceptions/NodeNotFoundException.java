package de.rallye.exceptions;

public class NodeNotFoundException extends WebAppExcept{

	public NodeNotFoundException() {
		super(404, "Node not found");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 770278077138594737L;

}
