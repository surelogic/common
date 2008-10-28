package com.surelogic.common.jobs.console;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitorFactory;
import com.surelogic.common.jobs.SLStatus;

/**
 * Class for submitting jobs to a "console." The jobs are not meant to be
 * interactive, and thus the console is really some kind of output-only
 * abstraction as encapsulated by the {@link SLProgressMonitor} used by jobs
 * submitted to this object. In reality, this progress monitor is further
 * abstracted away by using a {@link SLProgressMonitorFactory} that generates a
 * new progress monitor for each submitted job. For example, the class
 * {@link PrintWriterSLProgressMonitor} is meant to be used with this class.
 * <p>
 * Jobs are executed serially.
 * 
 * @see PrintWriterSLProgressMonitor
 */
public final class ConsoleJob {
	private final SLProgressMonitorFactory pmFactory;
	private final ExecutorService executor = Executors
			.newSingleThreadExecutor();

	private static final class ConsoleCallable implements Callable<SLStatus> {
		private final SLJob slJob;
		private final SLProgressMonitor monitor;

		public ConsoleCallable(final SLJob slj, final SLProgressMonitor pm) {
			slJob = slj;
			monitor = pm;
		}

		public SLStatus call() {
			return slJob.run(monitor);
		}
	}

	public ConsoleJob(final SLProgressMonitorFactory pmf) {
		pmFactory = pmf;
	}

	/**
	 * Execute the given job. Doesn't return until the job terminates.
	 * 
	 * @param slJob
	 *            The job to execute.
	 * @return The status of the job.
	 */
	public SLStatus submitJob(final SLJob slJob) {
		final SLProgressMonitor monitor = pmFactory
				.createSLProgressMonitor(slJob.getName());
		final Future<SLStatus> future = executor.submit(new ConsoleCallable(
				slJob, monitor));
		try {
			return future.get();
		} catch (InterruptedException e) {
			return SLStatus.createErrorStatus(e);
		} catch (ExecutionException e) {
			return SLStatus.createErrorStatus(e);
		}
	}
}
