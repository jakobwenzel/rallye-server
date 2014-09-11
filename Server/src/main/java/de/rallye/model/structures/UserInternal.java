package de.rallye.model.structures;


import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserInternal extends User {

	@JsonIgnore	final public int pushMode;
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
