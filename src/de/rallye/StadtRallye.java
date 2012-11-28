package de.rallye;

import java.beans.PropertyVetoException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		Logger logger =  LogManager.getLogger(StadtRallye.class.getName());
		//create and init new GameHandler
		GameHandler game = new GameHandler();
		
		//temporary to hold the server open
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.throwing(e);
			}
		}
		
		
		/*logger.info("Hit enter to stop server...");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		game.stopServer();
		//game.start();
		logger.exit();*/
	}

}
