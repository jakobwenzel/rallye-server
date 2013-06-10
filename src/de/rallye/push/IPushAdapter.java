package de.rallye.push;

import java.util.List;

import de.rallye.model.structures.UserInternal;


public interface IPushAdapter {

	void push(List<UserInternal> users, String payload);

}
