package com.surelogic.common.jdbc;

import java.sql.Timestamp;
import java.util.Date;

public interface Row {

	int nextInt();

	Integer nullableInt();

	long nextLong();

	Long nullableLong();

	String nextString();

	Date nextDate();

	Timestamp nextTimestamp();

	boolean nextBoolean();

	Boolean nullableBoolean();
}
