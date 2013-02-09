/**
 * 
 */
package de.rallye.pushService.resource;

/**
 * @author Felix Huebner
 * @date 18.12.12
 * @version 1.1
 *
 */
public class PushCommand {

	// chatroom commands
	public static final int CHATROOM_UPDATE = 100;
	
	// map commands
	public static final int MAP_UPDATE = 200;
	
	// group commands
	public static final int GROUP_UPDATE = 300;
	
	// gameplay
	public static final int GAME_UPDATE = 400;
	public static final int GAME_START = 401;
	public static final int GAME_END = 402;
	public static final int NEXT_ROUND = 403;
	
}
