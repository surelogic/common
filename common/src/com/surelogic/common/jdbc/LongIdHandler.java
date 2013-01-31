package com.surelogic.common.jdbc;

/**
 * A key handler for rows that have a column named ID with a generated
 * {@code long} value.
 * 
 * @author nathan
 * 
 */
public class LongIdHandler implements KeyHandler<Long> {

	@Override
  public Long handle(Row r) {
		return r.nextLong();
	}

	@Override
  public String[] keyNames() {
		return new String[] { "ID" };
	}

}
