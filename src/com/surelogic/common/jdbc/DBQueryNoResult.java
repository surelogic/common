package com.surelogic.common.jdbc;

/**
 * A helper implementation of {@link DBQuery} that returns no result.
 */
public abstract class DBQueryNoResult implements DBQuery<Void> {

	public final Void perform(Query q) {
		doPerform(q);
		return null;
	}

	abstract public void doPerform(Query q);
}
