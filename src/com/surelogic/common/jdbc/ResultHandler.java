package com.surelogic.common.jdbc;

public interface ResultHandler<T> {

	T handle(Result result);

}
