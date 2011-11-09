package com.surelogic.common.jdbc;

public interface RowHandler<T> {

	T handle(Row r);
	
}
