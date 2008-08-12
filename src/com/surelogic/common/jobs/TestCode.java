package com.surelogic.common.jobs;

public enum TestCode {
	NONE, LOW_MEMORY, HIGH_MEMORY, MAD_MEMORY, MISSING_CODE, BAD_AUX_PATH, BAD_CONFIG, 
	NO_TOOL_OUTPUT, SCAN_CANCELLED, SCAN_FAILED, ABNORMAL_EXIT, EXCEPTION;

	public static TestCode getTestCode(String code) {
		if (code == null) {
			return TestCode.NONE;
		}
		return TestCode.valueOf(code);
	}
}
