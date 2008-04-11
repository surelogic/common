package com.surelogic.common.logging;

public class LogStatus {
	private static IStatusDelegate delegate;
	
	public static synchronized void setDelegate(IStatusDelegate d) {
		delegate = d;
	}	
	
	public static synchronized Object createErrorStatus(int code, String message) {
		return delegate.createErrorStatus(code, message);
	}

	public static synchronized Object createErrorStatus(int code, String message, Exception e) {
		return delegate.createErrorStatus(code, message, e);
	}   
}
