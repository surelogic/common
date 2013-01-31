package com.surelogic.common.jdbc;

/**
 * This handler returns the first String in the first row of this result.
 * 
 * @author nathan
 * 
 */
public final class StringResultHandler implements ResultHandler<String> {

	@Override
  public String handle(final Result r) {
		for (final Row row : r) {
			return row.nextString();
		}
		return null;
	}

}
