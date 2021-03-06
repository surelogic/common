package com.surelogic.common.jdbc;

public class LongResultHandler implements ResultHandler<Long> {

	@Override
  public Long handle(final Result result) {
		for (final Row r : result) {
			return r.nullableLong();
		}
		return null;
	}

}
