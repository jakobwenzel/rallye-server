package de.rallye.injection;

import de.rallye.config.GitRepositoryState;
import de.rallye.config.RallyeConfig;
import de.rallye.db.DataAdapter;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.GameState;
import de.rallye.push.PushService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HK2 Binder, declaring all dependencies of  injectable Objects
 *
 * WARNING: RallyeConfigFactory has to be initialized separately
 */
public class RallyeBinder extends AbstractBinder {

	private static Logger logger = LogManager.getLogger(RallyeBinder.class);

	public static DataAdapter data;
	public static RallyeConfig config;


	private static class ChatPictureMap extends TypeLiteral<Map<String, ChatPictureLink>> {

	}


	@Override
	protected void configure() {

		bind(GitRepositoryState.class).to(GitRepositoryState.class);
		bind(config).to(RallyeConfig.class);
		bind(ImageRepository.class).to(ImageRepository.class).in(Singleton.class);

		bind(Collections.synchronizedMap(new HashMap<String, ChatPictureLink>())).to(new ChatPictureMap());

//		bindFactory(DataAdapterFactory.class).to(DataAdapter.class);
		bind(data).to(DataAdapter.class);
		bind(GameState.class).to(GameState.class).in(Singleton.class);
		bind(PushService.class).to(PushService.class).in(Singleton.class);
	}
}
