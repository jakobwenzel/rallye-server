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

import de.rallye.model.structures.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static de.rallye.model.calendar.Calendar.getTime;

/**
 * Created by Ramon on 21.09.2014.
 */
@Path("resources/calendar")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Calendar {

	private static final Logger logger =  LogManager.getLogger(Calendar.class.getName());

	@GET
	public de.rallye.model.calendar.ICalendar getCalendar(@Context Request request) {
		logger.entry();
//		File file = new File();
//		HttpCacheHandling.checkModifiedSince(request, file.lastModified());

		List<de.rallye.model.calendar.Calendar.Day> days = new ArrayList<>();
		List<de.rallye.model.calendar.ICalendar.IEvent> events = new ArrayList<>();
		events.add(new de.rallye.model.calendar.Calendar.Event(getTime(950), getTime(1035), "Begrüßung", null, Color.YELLOW.getRGB(), new Location(null, "S1|01 A4")));
		HashMap<Integer, Location> locationMap = new HashMap<>();
		locationMap.put(1, new Location(null, "S3|06 321"));
		locationMap.put(2, new Location(null, "S2|02 C110"));
		events.add(new de.rallye.model.calendar.Calendar.GroupSpecificEvent(getTime(1140), getTime(1250), "Organisatorisches und Kennenlernen", null, Color.PINK.getRGB(), locationMap));
		days.add(new de.rallye.model.calendar.Calendar.Day(0, events));

		return logger.exit(new de.rallye.model.calendar.Calendar(days, Date.parse("Mon, 29 Sep 2014 00:00:00 GMT+2")));
	}
}
