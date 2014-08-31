/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.filter.auth;

import javax.xml.bind.DatatypeConverter;

/**
 * Allow to encode/decode the authentification
 * @author Deisss (LGPLv3)
 */
public class BasicAuth {
    /**
     * Decode the basic auth and convert it to array login/password
     * @param auth The string encoded authentification
     * @return The login (case 0), the password (case 1)
     */
    public static String[] decode(String auth) {
    	try {
	        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
	        auth = auth.replaceFirst("[B|b]asic ", "");
	 
	        //Decode the Base64 into byte[]
	        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);
	 
	        //If the decode fails in any case
	        if(decodedBytes == null || decodedBytes.length == 0){
	            return null;
	        }
	 
	        //Now we can convert the byte[] into a splitted array :
	        //  - the first one is login,
	        //  - the second one password
	        return new String(decodedBytes).split(":", 2);
    	} catch (Exception e) {
    		return null;
    	}
    }
}