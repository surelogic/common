package com.surelogic.common.jdbc;

//TODO I think we can enforce better type safety than this
public interface RecordMapper {

	public void insert(AbstractRecord<?> record);

	public void remove(AbstractRecord<?> record);

	public boolean select(AbstractRecord<?> record);

}