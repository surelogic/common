package com.surelogic.common.jdbc;

public class IntegerResultHandler implements ResultHandler<Integer> {

	@Override
  public Integer handle(final Result result) {
		for (final Row r : result) {
			return r.nextInt();
		}
		return null;
	}

}
