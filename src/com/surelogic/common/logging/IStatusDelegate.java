package com.surelogic.common.logging;

public interface IStatusDelegate {
	Object createErrorStatus(int code, String message);
	Object createErrorStatus(int code, String message, Exception e);
}
