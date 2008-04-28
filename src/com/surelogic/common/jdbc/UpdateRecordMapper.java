package com.surelogic.common.jdbc;



public interface UpdateRecordMapper extends RecordMapper {

	public void update(AbstractUpdatableRecord<?> record);

}