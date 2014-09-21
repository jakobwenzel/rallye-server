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

import de.rallye.annotations.KnownUserAuth;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.ServerInfo;
import de.rallye.util.HttpCacheHandling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Path("server")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Server {

	public static final int API_VERSION = 4;
	public static final String API_NAME = "server";

	private final Logger logger = LogManager.getLogger(Server.class);
	
	@Inject	RallyeConfig config;
	@Inject	IDataAdapter data;
	
	@GET
	@Path("info")
	public ServerInfo getDescription(@Context Request request) {
		HttpCacheHandling.checkModifiedSince(request, config.lastModified());

		return config.getServerInfo();
	}

	@GET
	@Path("status")
	@KnownUserAuth
	public Response getStatus() {
		throw new UnsupportedOperationException();//TODO
	}
	
	@GET
	@Path("pushModes")
	public List<PushMode> getPushModes() throws DataException {
		logger.entry();
	
		List<PushMode> res = data.getPushModes();
		return logger.exit(res);
	}

	@GET
	@Path("picture")
	@Produces("image/jpeg")
	public File getPicture(@Context Request request) throws FileNotFoundException {
		File picture = new File(config.getDataDirectory()+"game/picture.jpg");

		HttpCacheHandling.checkModifiedSince(request, picture.lastModified());

		if (picture.exists())
			return picture;
		else throw new FileNotFoundException("Picture not found");
	}
	
	@GET
	@Path("rallye.apk")
	@Produces("application/vnd.android.package-archive")
	public File getApp() throws FileNotFoundException{
		logger.entry();
		File f = new File(config.getDataDirectory()+"rallye.apk");
		if (f.exists())
			return logger.exit(f);
		else throw new FileNotFoundException("Apk not found");
	}
}
