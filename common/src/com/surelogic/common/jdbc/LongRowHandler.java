package com.surelogic.common.jdbc;

public class LongRowHandler implements RowHandler<Long> {

	@Override
  public Long handle(final Row r) {
		return r.nullableLong();
	}

}
