package com.surelogic.common.jdbc;

public final class StringResultHandler implements ResultHandler<String> {

	public String handle(Result r) {
		for (final Row row : r) {
			return row.nextString();
		}
		return null;
	}

}
