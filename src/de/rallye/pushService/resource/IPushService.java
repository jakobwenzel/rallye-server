/**
 * 
 */
package de.rallye.pushService.resource;

import java.util.List;
import java.util.Map;

/**
 * @author felix
 * 
 */
public interface IPushService {

	/**
	 * this method push the info to the clients
	 * 
	 * @param lst
	 *            List of Strings of Clients
	 * @param lstChanges
	 *            after the execution of this method the invalid clients will be
	 *            in this list, the first string is the oldID, the second is the
	 *            newID, if the second ID is "null" the client must be removed
	 * @param type
	 *            Command Type to send to the clients
	 * @param value
	 *            value to send to the clients
	 */
	public void push(List<String> lst, Map<String, String> lstChanges,
			int type, String value);

	/**
	 * this method push the info to the given client
	 * 
	 * @param client
	 *            the id of the client
	 * @param lstChanges
	 *            after the execution of this method the invalid clients will be
	 *            in this list, the first string is the oldID, the second is the
	 *            newID, if the second ID is "null" the client must be removed
	 * @param type
	 *            Command Type to send to the client
	 * @param value
	 *            value to send to the client
	 */
	public void push(String client, Map<String, String> lstChanges, int type,
			String value);
}
