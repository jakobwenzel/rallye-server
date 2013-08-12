package de.rallye.model.structures;

import org.codehaus.jackson.annotate.JsonIgnore;

public class UserInternal extends User {

	@JsonIgnore final public int pushMode;
	@JsonIgnore final public String pushID;

	public UserInternal(int userID, String name, int pushMode, String pushID) {
		super(userID, name);
		
		this.pushMode = pushMode;
		this.pushID = pushID;
	}

	@Override
	public String toString() {
		return super.toString() +"("+ pushMode +")";
	}
}
