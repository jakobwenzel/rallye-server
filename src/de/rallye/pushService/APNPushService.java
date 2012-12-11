/**
 * 
 */
package de.rallye.pushService;

import java.util.List;
import java.util.Map;

import de.rallye.pushService.resource.IPushService;

/**
 * @author Felix Huebner
 * @date 11.12.2012
 *
 */
public class APNPushService implements IPushService {

	private static String APN_Key;
	/**
	 * 
	 */
	public APNPushService(String APNKey) {
		// TODO Auto-generated constructor stub
		APN_Key = APNKey;
	}

	/* (non-Javadoc)
	 * @see de.rallye.pushService.resource.IPushService#push(java.util.List, java.util.Map, int, java.lang.String)
	 */
	@Override
	public void push(List<String> lst, Map<String, String> lstChanges,
			int type, String value) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see de.rallye.pushService.resource.IPushService#push(java.lang.String, java.util.Map, int, java.lang.String)
	 */
	@Override
	public void push(String client, Map<String, String> lstChanges, int type,
			String value) {
		// TODO Auto-generated method stub

	}

}
