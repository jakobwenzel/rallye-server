package de.rallye.exceptions;

public class DataException extends Exception {

	private static final long serialVersionUID = 1L;

	
	public DataException(Throwable e) {
		super("Data Retrieval failed", e);
	}


	public DataException(String string) {
		super(string);
	}
}
