package com.surelogic.common.jobs.remote;

public interface ILocalConfig {
	String getPluginDir(String id, boolean required);
	int getMemorySize();
	String getTestCode();
	boolean isVerbose();

	/**
	 * @return A path to where you want the console output to go
	 */
	String getLogPath();
}
