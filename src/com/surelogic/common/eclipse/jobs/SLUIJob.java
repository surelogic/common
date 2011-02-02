package com.surelogic.common.eclipse.jobs;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * An abstract class that makes extending {@link UIJob} simpler to extend by not
 * requiring a job name.
 */
public abstract class SLUIJob extends UIJob {

	public SLUIJob(Display jobDisplay, String name) {
		super(jobDisplay, name);
		this.setPriority(INTERACTIVE);
	}

	public SLUIJob(String name) {
		super(name);
		this.setPriority(INTERACTIVE);
	}

	public SLUIJob() {
		super("SureLogic, Inc. User Interface Job");
		this.setSystem(true);
		this.setPriority(INTERACTIVE);
	}
}
