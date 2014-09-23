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

package de.rallye.api;

import de.rallye.StadtRallye;
import de.rallye.config.ConfigTools;
import de.rallye.config.RallyeConfig;
import de.rallye.exceptions.WebAppExcept;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Path("resources/webclient")
public class Webclient {
	private static final String RESOURCE_PATH = "webclient/";

	private static final Logger logger =  LogManager.getLogger(Webclient.class);
	
	private static boolean projectChecked = false;
	private static String projectDir;
	
	private static final Map<String,String> mime = new HashMap<String,String>();
	static {
		mime.put("js","application/javascript");
		mime.put("html","text/html");
		mime.put("htm","text/html");
		mime.put("gif","image/gif");
	}

	@Inject	RallyeConfig config;

	@GET
	@Path("{path}")
	public Response index(@PathParam("path") String path, @Context SecurityContext sec) {
		
		if (path.contains("/"))
			throw new WebAppExcept("Path invalid", 404);
		
		logger.debug("Trying to load "+RESOURCE_PATH+path);
		
		//If we are running from project dir, always load files from src directory to avoid recompiles during development
		if (!projectChecked) {
			projectDir = ConfigTools.getProjectDir();
			projectChecked = true;
		}
		
		InputStream stream;
		if (projectDir!=null) {
			logger.debug("serving from project dir");
			try {
				stream = new FileInputStream(new File(new URL(projectDir+"Server/src/main/resources/de/rallye/"+RESOURCE_PATH+path).toURI()));
			} catch(Exception e) {
				throw new WebAppExcept(e);
			}
		} else {
		
			logger.debug("serving from jar");
			stream = StadtRallye.class.getResourceAsStream(RESOURCE_PATH+path);
		}
		
		if (stream==null) {

			throw new WebAppExcept("Not found.", 404);
		}

		String ext = path.substring(path.lastIndexOf('.')+1);
		String mediaType = mime.get(ext);
		if (mediaType==null)
			mediaType="application/octet-stream";
		
		return Response.ok(stream,mediaType).build();
	
	}
	
	@GET
	public Response index(@Context SecurityContext sec) {
		
		return index("index.html",sec);
		
	}
}
