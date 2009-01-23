package com.surelogic.common.jdbc;

/**
 * A simple handler that returns true if the query returns any results.
 * 
 * @author nathan
 * 
 */
public class HasResultHandler implements ResultHandler<Boolean> {
	public Boolean handle(final Result result) {
		return result.iterator().hasNext();
	}

}
