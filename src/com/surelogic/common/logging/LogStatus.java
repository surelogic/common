package com.surelogic.common.logging;

public class LogStatus {
	private static IStatusDelegate delegate;
	
	public static synchronized void setDelegate(IStatusDelegate d) {
		delegate = d;
	}	
	
	public static synchronized void createErrorStatus(int code, String message) {
		delegate.createErrorStatus(code, message);
	}   
}
