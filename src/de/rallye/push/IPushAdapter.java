package de.rallye.push;

import java.util.List;

import de.rallye.model.structures.UserInternal;
import de.rallye.model.structures.PushEntity.Type;


public interface IPushAdapter {

	void push(List<UserInternal> users, String payload, Type type);

}
