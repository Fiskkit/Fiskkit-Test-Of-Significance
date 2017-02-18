package com.fiskkit.exception;

public class FiskkitException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FiskkitException(String msg){
		super(msg);
	}
	
	public FiskkitException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
	public FiskkitException(Throwable throwable){
		super(throwable);
	}

}
