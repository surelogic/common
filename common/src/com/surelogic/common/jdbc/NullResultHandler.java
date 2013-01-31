package com.surelogic.common.jdbc;

/**
 * A {@link ResultHandler} that does not return a value.
 * 
 * @author nathan
 * 
 */
public abstract class NullResultHandler implements ResultHandler<Void> {

	@Override
  public final Void handle(final Result result) {
		doHandle(result);
		return null;
	}

	protected abstract void doHandle(Result result);

}
