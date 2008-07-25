package com.surelogic.common.jobs;


/**
 * Interface implemented by IDE independent jobs. Work such as database queries
 * can be done in jobs like this. The monitoring and returned status of these
 * jobs is independent of any particular IDE.
 */
public interface SLJob {

	/**
	 * Executes this job. Returns the result of the execution.
	 * 
	 * the monitor to be used for reporting progress and responding to
	 * cancelation. The monitor is never {@code null}.
	 * 
	 * @return resulting status of the run. The result must not be {@code null}.
	 */
	SLStatus run(SLProgressMonitor monitor);
}
