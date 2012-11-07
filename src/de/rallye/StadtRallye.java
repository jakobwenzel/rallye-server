package de.rallye;

import java.beans.PropertyVetoException;
import java.io.IOException;

import de.rallye.control.GameHandler;

/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class StadtRallye {

	/**
	 * @param args
	 * @author Felix HŸbner
	 * @throws PropertyVetoException 
	 */
	public static void main(String[] args) throws PropertyVetoException {
		//create and init new GameHandler
		GameHandler game = new GameHandler();
		
		//temporary to hold the server open
		System.out.println("Hit enter to stop server...");
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
