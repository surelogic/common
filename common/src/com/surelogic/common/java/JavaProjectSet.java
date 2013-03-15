package com.surelogic.common.java;

import java.io.File;

/**
 * A set for projects specified for a run/scan
 * 
 * @author edwin
 */
public abstract class JavaProjectSet<P> implements Iterable<P> {

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<Config> getConfigs() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getRunDir() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRun() {
		return null;
	}

	public void mapToProject(File f, String name) {
		// TODO Auto-generated method stub
		
	}

	public P add(Config config) {
		// TODO Auto-generated method stub
		return null;
	}

	public P get(String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetOrdering() {
		// TODO Auto-generated method stub
		
	}

	public String checkMapping(File f) {
		// TODO Auto-generated method stub
		return null;
	}
}
