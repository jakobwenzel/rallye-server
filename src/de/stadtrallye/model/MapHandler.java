package de.stadtrallye.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Felix HŸbner
 * @version 1.0
 *
 */
public class MapHandler {
	
	//DataHandler dh = null;

	/**
	 * constructor
	 * @param #
	 */
	/*public MapHandler(DataHandler dataHandler) {
		this.dh = dataHandler;
	}*/

	
	public static JSONObject getAllNodes(DataHandler dh) {
		JSONObject a = new JSONObject();
		try {
			a.put("name", "Hobbit");
			a.put("lat", "49.877572");
			a.put("long", "8.658277");
			a.put("discription","null");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;
	}
}
