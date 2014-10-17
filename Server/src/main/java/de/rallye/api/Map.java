/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.api;

import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.MapConfig;
import de.rallye.model.structures.Node;
import de.rallye.util.HttpCacheHandling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import java.util.Collection;
import java.util.List;

@Path("games/map")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Map {
	
	private static final Logger logger =  LogManager.getLogger(Map.class.getName());

	@Inject	IDataAdapter data;
	@Inject	RallyeConfig config;

	@GET
	@Path("nodes")
	public Collection<Node> getNodes(@Context Request request) {
		logger.entry();

		HttpCacheHandling.checkModifiedSince(request, data.getNodesLastModified());
		
		Collection<Node> res = data.getNodes().values();
		return logger.exit(res);
	}
	
	@GET
	@Path("edges")
	public List<Edge> getEdges(@Context Request request) {
		logger.entry();

		HttpCacheHandling.checkModifiedSince(request, data.getEdgesLastModified());
		
		List<Edge> res = data.getEdges();
		return logger.exit(res);
	}

	@GET
	@Path("config")
	public MapConfig getConfig(@Context Request request) {
		logger.entry();

		HttpCacheHandling.checkModifiedSince(request, config.lastModified());

		MapConfig res = config.getMapConfig();
		return logger.exit(res);
	}
}