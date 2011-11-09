package com.surelogic.common.core.jobs;

import org.eclipse.core.runtime.jobs.Job;

/**
 * Defines an Eclipse job that accesses a SureLogic database.
 */
public abstract class DatabaseJob extends Job {

	final Object family;

	/**
	 * Constructs a database job.
	 * 
	 * @param name
	 *            a name for the job.
	 * @param accessKey
	 *            an access key for a particular databases. Jobs with the same
	 *            access keys will proceed in serial order.
	 */
	public DatabaseJob(String name, String accessKey) {
		this(name, BUILD, accessKey);
	}

	/**
	 * Constructs a database job.
	 * 
	 * @param name
	 *            a name for the job.
	 * @param accessKeys
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 */
	public DatabaseJob(String name, String... accessKeys) {
		this(name, BUILD, accessKeys);
	}

	/**
	 * Constructs a database job.
	 * 
	 * @param name
	 *            a name for the job.
	 * @param priority
	 *            the new job priority. One of INTERACTIVE, SHORT, LONG, BUILD,
	 *            or DECORATE as declared in
	 *            {@link org.eclipse.core.runtime.jobs.Job}.
	 * @param accessKey
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 */
	public DatabaseJob(String name, int priority, String accessKey) {
		this(null, name, priority, accessKey);
	}

	/**
	 * Constructs a database job.
	 * 
	 * @param name
	 *            a name for the job.
	 * @param priority
	 *            the new job priority. One of INTERACTIVE, SHORT, LONG, BUILD,
	 *            or DECORATE as declared in
	 *            {@link org.eclipse.core.runtime.jobs.Job}.
	 * @param accessKeys
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 */
	public DatabaseJob(String name, int priority, String... accessKeys) {
		this(null, name, priority, accessKeys);
	}

	/**
	 * Constructs a database job.
	 * 
	 * @param family
	 *            the job family, may be {@code null}.
	 * @param name
	 *            the non-null name of the job.
	 * @param priority
	 *            the new job priority. One of INTERACTIVE, SHORT, LONG, BUILD,
	 *            or DECORATE as declared in
	 *            {@link org.eclipse.core.runtime.jobs.Job}.
	 * @param accessKey
	 *            an access key for a particular databases. Jobs with the same
	 *            access keys will proceed in serial order.
	 */
	public DatabaseJob(Object family, String name, int priority,
			String accessKey) {
		super(name);
		this.family = family;
		setRule(KeywordAccessRule.getInstance(accessKey));
		setPriority(priority);
	}

	/**
	 * Constructs a database job.
	 * 
	 * @param family
	 *            the job family, may be {@code null}.
	 * @param name
	 *            the non-null name of the job.
	 * @param priority
	 *            the new job priority. One of INTERACTIVE, SHORT, LONG, BUILD,
	 *            or DECORATE as declared in
	 *            {@link org.eclipse.core.runtime.jobs.Job}.
	 * @param accessKeys
	 *            a list of access keys to particular databases. Jobs with the
	 *            same access keys will proceed in serial order.
	 */
	public DatabaseJob(Object family, String name, int priority,
			String... accessKeys) {
		super(name);
		this.family = family;
		setRule(KeywordAccessRule.getInstance(accessKeys));
		setPriority(priority);
	}

	@Override
	public boolean belongsTo(Object family) {
		return this.family == family;
	}
}
