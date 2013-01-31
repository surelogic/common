package com.surelogic.common.jdbc;

/**
 * A helper implementation of {@link DBQuery} that returns no result.
 */
public abstract class NullDBQuery implements DBQuery<Void> {

	@Override
  public final Void perform(final Query q) {
		doPerform(q);
		return null;
	}

	abstract public void doPerform(Query q);
}
