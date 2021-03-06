package com.surelogic.common.jdbc;

import java.sql.SQLException;


/**
 * An UpdatableRecord has all of the same behavior as {@link Record} and can
 * also be updated. All rows of a record are allowed to change except for it's
 * primary key.
 * 
 * @author nathan
 * 
 * @param <T>
 * @see ConnectionQuery#record(Class)
 */
public interface UpdatableRecord<T> extends Record<T> {
	/**
	 * Update the record's row on the database.
	 * 
	 * @throws SQLException
	 */
	void update();
}
