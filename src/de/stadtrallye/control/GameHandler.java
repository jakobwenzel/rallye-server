package de.stadtrallye.control;

import de.stadtrallye.model.DataHandler;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class GameHandler {
	DataHandler data;
	ClientListener listener;
	
	public GameHandler() {
	// create and init new DataHander
	data = new DataHandler();
	
	//create and init new ClientListener,
	listener = new ClientListener(10101);
	}
}
