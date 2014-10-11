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
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye;

import com.fasterxml.jackson.jaxrs.smile.JacksonSmileProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import de.rallye.injection.RallyeBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

import javax.ws.rs.ApplicationPath;

/**
 * Created by Ramon on 02.10.2014.
 */
@ApplicationPath("/")
public class RallyeApplication extends ResourceConfig {

	public RallyeApplication() {
		packages("de.rallye.api", "de.rallye.filter.auth", "de.rallye.exceptions.mappers");
		register(JacksonSmileProvider.class);
		register(JacksonXMLProvider.class);
		register(JacksonFeature.class);
		//register(EnsureMimeType.class);
		register(new RallyeBinder());
		register(MultiPartFeature.class);

        property(MvcFeature.TEMPLATE_BASE_PATH, "templates");
		property(MustacheMvcFeature.CACHE_TEMPLATES, false); //TODO enable caching once templates are no longer being modified
        register(MustacheMvcFeature.class);
	}
}
