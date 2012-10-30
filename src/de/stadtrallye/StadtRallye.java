package de.stadtrallye;

import java.io.IOException;

import de.stadtrallye.control.GameHandler;

/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class StadtRallye {

	/**
	 * @param args
	 * @author Felix HŸbner
	 */
	public static void main(String[] args) {
		//create and init new GameHandler
		GameHandler game = new GameHandler();
		
		
		//temporary to hold the server open
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		game.stopServer();
		//game.start();
		
	}

}
