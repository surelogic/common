package com.surelogic.common.jdbc;

public class LongRowHandler implements RowHandler<Long> {

	public Long handle(final Row r) {
		return r.nullableLong();
	}

}
