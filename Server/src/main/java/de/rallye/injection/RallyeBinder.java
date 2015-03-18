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

package de.rallye.injection;

import de.rallye.admin.AdminWebsocketApp;
import de.rallye.config.ConfigTools;
import de.rallye.config.GitRepositoryState;
import de.rallye.config.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.db.IDataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.GameState;
import de.rallye.model.structures.RallyeGameState;
import de.rallye.model.structures.SubmissionPictureLink;
import de.rallye.push.PushService;
import de.rallye.push.PushWebsocketApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HK2 Binder, declaring all dependencies of injectable Objects
 *
 */
public class RallyeBinder extends AbstractBinder {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(RallyeBinder.class);

	public static IDataAdapter data;
	public static RallyeConfig config;
	public static RallyeGameState gameState;
	public static boolean isServlet = false;


	private static class ChatPictureMap extends TypeLiteral<Map<String, ChatPictureLink>> {

	}

	private static class SubmissionPictureMap extends TypeLiteral<Map<String, SubmissionPictureLink>> {

	}


	@Override
	protected void configure() {
		if (config == null || isServlet) {
			File cfgFile = ConfigTools.findConfigFile();

			if (isServlet && cfgFile != null) {
				logger.info("Overriding static config with user config");
				config = null;
			}

			if (config == null) {
				config = RallyeConfig.fromFile(cfgFile, GitRepositoryState.getState());
			}

			if (config == null)
				throw new RuntimeException("Instantiating RallyeBinder without RallyeConfig");
		}
		logger.info("RallyeConfig initialized to {}", config);
		if (data == null) {
			logger.info("No DataAdapter yet...");
			data = DataAdapter.getInstance(config);
			PushWebsocketApp.setData(data);
			AdminWebsocketApp.setData(data);
		}
		logger.info("Data initialized to {}", data);
		if (gameState == null) {
			logger.info("No GameState yet");
			gameState = RallyeGameState.getInstance(data);
		}
		logger.info("GameState initialized to {}", gameState);

		bind(isServlet).named("IsServlet");

		bind(GitRepositoryState.class).to(GitRepositoryState.class);
		bind(config).to(RallyeConfig.class);
		bind(ImageRepository.class).to(ImageRepository.class).in(Singleton.class);

		bind(Collections.synchronizedMap(new HashMap<String, ChatPictureLink>())).to(new ChatPictureMap());
		bind(Collections.synchronizedMap(new HashMap<String, SubmissionPictureLink>())).to(new SubmissionPictureMap());

//		bindFactory(DataAdapterFactory.class).to(DataAdapter.class);
		bind(data).to(IDataAdapter.class);
		bind(GameState.class).to(GameState.class).in(Singleton.class);
		bind(PushService.class).to(PushService.class).in(Singleton.class);

		if (gameState==null)
			logger.warn("GameState is null");
		bind(gameState).to(RallyeGameState.class);
	}
}
