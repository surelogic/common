package com.surelogic.common.jobs.remote;

import java.io.File;
import java.util.logging.Level;

import com.surelogic.common.SLUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.logging.SLLogger;

public abstract class AbstractLocalConfig implements ILocalConfig {
	final int memSize;
	final File runDir;

	protected AbstractLocalConfig(int mem, File dir) {
		memSize = mem;
		runDir = dir;
	}
	
	public final int getMemorySize() {
		return memSize;
	}

	public String getTestCode() {
		return TestCode.NONE.name();
	}

	public boolean isVerbose() {
		return SLLogger.getLogger().isLoggable(Level.FINE) || XUtil.testing;
	}
	
	public final String getLogPath() {
		return new File(runDir, SLUtility.LOG_NAME).getAbsolutePath();
	}
	
	public final String getRunDirectory() {
		return runDir.getAbsolutePath();
	}
}
