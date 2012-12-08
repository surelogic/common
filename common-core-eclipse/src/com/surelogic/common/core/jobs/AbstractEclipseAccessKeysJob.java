package com.surelogic.common.core.jobs;

import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.NonNull;

/**
 * An extension to {@link Job} that supports a list of access keys to particular
 * resources, such as a database. Jobs with the same access keys will proceed in
 * serial order.
 */
public abstract class AbstractEclipseAccessKeysJob extends Job {

  /**
   * Constructs a job with an optional set of access keys. Jobs with the same
   * access keys will proceed in serial order.
   * 
   * @param name
   *          the name of this job.
   * @param accessKeys
   *          a list of access keys to particular resources, such as a database.
   *          Jobs with the same access keys will proceed in serial order. If no
   *          access keys are passed no serialization rule will be setup.
   */
  protected AbstractEclipseAccessKeysJob(@NonNull String name, String... accessKeys) {
    super(name);
    if (accessKeys.length > 0)
      setRule(KeywordAccessRule.getInstance(accessKeys));
  }
}
