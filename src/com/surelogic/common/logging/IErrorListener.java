package com.surelogic.common.logging;

public interface IErrorListener {
	void reportError(String summary, String msg);
	
	IErrorListener throwListener = new IErrorListener() {
		@Override
		public void reportError(String summary, String msg) {
			throw new IllegalStateException(msg);
		}
	};
}
