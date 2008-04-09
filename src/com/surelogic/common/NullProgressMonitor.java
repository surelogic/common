package com.surelogic.common;

import com.surelogic.common.SLProgressMonitor;

public class NullProgressMonitor implements SLProgressMonitor {

	private static final NullProgressMonitor singleton = new NullProgressMonitor();

	public void beginTask(String name, int totalWork) {
		// Do nothing

	}

	public void done() {
		// Do nothing

	}

	public void internalWorked(double work) {
		// Do nothing

	}

	public boolean isCanceled() {
		// Do nothing
		return false;
	}

	public void setCanceled(boolean value) {
		// Do nothing

	}

	public void setTaskName(String name) {
		// Do nothing

	}

	public void subTask(String name) {
		// Do nothing

	}

	public void worked(int work) {
		// Do nothing
	}

	public static SLProgressMonitor instance() {
		return singleton;
	}

	public void failed(String msg) {
		// Do nothing
	}

	public void failed(Throwable t) {
		// Do nothing
	}

	public Throwable getFailureTrace() {
		return null;
	}

	public void error(String msg) {
		// Do nothing
	}

	public void error(String msg, Throwable t) {
		// Do nothing
	}

	public void failed(String msg, Throwable t) {
		// Do nothing
	}
}

