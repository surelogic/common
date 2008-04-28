package com.surelogic.common.jdbc;

public class EmptyResultHandler implements ResultHandler<Void> {

	public Void handle(Result r) {
		return null;
	}

}
