package com.surelogic.common.jdbc;

class EmptyResultHandler implements ResultHandler<Void> {

	public Void handle(final Result r) {
		return null;
	}

}
