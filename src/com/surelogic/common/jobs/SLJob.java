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
	 * @param monitor
	 *            the monitor to be used for reporting progress and responding
	 *            to cancellation. It is recommended (as a defensive coding
	 *            practice) that the implementation check if the monitor is null
	 *            and construct a {@link NullSLProgressMonitor} in that case.
	 *            For example:
	 * 
	 *            <pre>
	 * if (monitor == null)
	 * 	monitor = new NullSLProgressMonitor();
	 * </pre>
	 * 
	 * @return resulting status of the run. The result must not be {@code null}.
	 * 
	 * @see NullSLProgressMonitor
	 */
	SLStatus run(SLProgressMonitor monitor);
}
