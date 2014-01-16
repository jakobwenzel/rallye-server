package de.rallye.model.structures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;

/**
 * Created by wilson on 05.10.13.
 */
public class RallyeGameState {

	private static final Logger logger =  LogManager.getLogger(RallyeGameState.class);

	protected RallyeConfig config;
	static private RallyeGameState instance;
	protected boolean showRatingToUsers;
	protected boolean canSubmit;

	public boolean isShowRatingToUsers() {
		return showRatingToUsers;
	}

	public boolean isCanSubmit() {
		return canSubmit;
	}

	/**
	 * Create a new GameState. Only to be called by DataAdapter
	 * @param showRatingToUsers
	 */
	@JsonCreator
	public RallyeGameState(@JsonProperty("showRatingToUsers") boolean showRatingToUsers, @JsonProperty("canSubmit") boolean canSubmit) {

		this.showRatingToUsers = showRatingToUsers;
		this.canSubmit = canSubmit;

	}
	public static RallyeGameState getInstance(IDataAdapter data) {
		if (instance==null) {
			try {
				instance = data.loadGameState();
			} catch (DataException e) {
				logger.error(e);
				instance = null;
			}
			if (instance==null)
				instance = new RallyeGameState(false, true);
		}

		return instance;
	}

	public void copy(RallyeGameState state) {
		this.canSubmit = state.canSubmit;
		this.showRatingToUsers = state.showRatingToUsers;
	}
}
