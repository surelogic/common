package com.surelogic.common.jdbc;

public interface DBQuery<T> {
	T perform(Query q);
}
