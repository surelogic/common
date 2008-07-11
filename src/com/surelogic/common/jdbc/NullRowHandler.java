package com.surelogic.common.jdbc;

/**
 * A {@link RowHandler} that returns no value.
 * 
 * @author nathan
 * 
 */
public abstract class NullRowHandler implements RowHandler<Void> {

	public final Void handle(final Row r) {
		doHandle(r);
		return null;
	}

	protected abstract void doHandle(Row r);

}
