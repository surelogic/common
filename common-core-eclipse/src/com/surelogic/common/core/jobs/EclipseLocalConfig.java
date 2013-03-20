package com.surelogic.common.core.jobs;

import java.io.File;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.jobs.remote.AbstractLocalConfig;

public class EclipseLocalConfig extends AbstractLocalConfig {
	public EclipseLocalConfig(int mem, File dir) {
		super(mem, dir);
	}

	@Override
	public String getPluginDir(String id, boolean required) {
		try {
			return EclipseUtility.getDirectoryOf(id);
		} catch (IllegalStateException e) {
			if (required) {
				throw e;
			} else {
				return null;
			}
		}
	}	
}
