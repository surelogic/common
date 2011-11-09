package com.surelogic.common.jobs;

/**
 * Interface implemented by IDE independent jobs. The monitoring and returned
 * status of these jobs is independent of any particular IDE.
 * <p>
 * It is recommended that jobs extend {@link AbstractSLJob} rather than
 * implement this interface directly.
 */
public interface SLJob {

	/**
	 * Gets the name of this job.
	 * 
	 * @return the name of this job, or {@code null} if the job has no name.
	 */
	String getName();

	/**
	 * Executes this job. Returns the result of the execution.
	 * 
	 * @param monitor
	 *            the monitor to be used for reporting progress and responding
	 *            to cancellation. This parameter should never be {@code null}.
	 * 
	 * @return resulting status of the run. The result must not be {@code null}.
	 * 
	 * @see NullSLProgressMonitor
	 */
	SLStatus run(SLProgressMonitor monitor);
}
