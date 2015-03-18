/*
 * Copyright (c) 2015 Jakob Wenzel, Ramon Wirsch.
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

package de.rallye.servlet;

import de.rallye.config.RallyeConfig;
import de.rallye.injection.RallyeBinder;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by Ramon on 18.03.2015.
 */
public class ServletInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		try {
			InputStream stream = ctx.getResourceAsStream("/WEB-INF/config.json");
			if (stream == null) {
				System.err.println(ctx.getResource("/WEB-INF/config.json").toString() + " not found");
			}
			RallyeBinder.config = RallyeConfig.fromStream(stream);
		} catch (Exception e) {
			System.err.println("Failed loading config from ServletContainer");
		}
	}
}
