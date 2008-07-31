package com.surelogic.common.jobs;

/**
 * An abstract implementation of {@link SLJob} that provides the job name via
 * its constructor.
 */
public abstract class AbstractSLJob implements SLJob {

	private final String f_name;

	public String getName() {
		return f_name;
	}

	/**
	 * Constructs and instance with the specified job name.
	 * 
	 * @param name
	 *            the name of this job, or {@code null} if the job has no name.
	 */
	protected AbstractSLJob(final String name) {
		f_name = name;
	}
}
