package com.surelogic.common.jobs.console;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitorFactory;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.SubSLProgressMonitor;

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
		if (executor.isShutdown())
			throw new IllegalStateException(
					ConsoleJob.class.getName()
							+ " is already shutdown and cannot accept further jobs to run");

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

	/**
	 * Indicates that no more jobs will be submitted to this instance.
	 */
	public void shutdown() {
		executor.shutdown();
	}

	/**
	 * A simple test top(sub(fifty-fifty)
	 */
	public static void main(String[] args) {
		final PrintWriter out = new PrintWriter(System.out);
		final ConsoleJob cj = new ConsoleJob(PrintWriterSLProgressMonitor
				.getFactory(out));
		final SLJob fifty = new SLJob() {

			public String getName() {
				return "fifty";
			}

			public SLStatus run(SLProgressMonitor monitor) {
				monitor.begin(50);
				for (int i = 0; i < 50; i++)
					monitor.worked(1);
				monitor.done();
				return SLStatus.OK_STATUS;
			}
		};
		final SLJob sub = new SLJob() {

			public String getName() {
				return "sub";
			}

			public SLStatus run(SLProgressMonitor monitor) {
				monitor.begin(120);
				AbstractSLJob.invoke(fifty, monitor, 10);
				AbstractSLJob.invoke(fifty, monitor, 100);
				// skip 10 as the done should do this
				monitor.done();
				return SLStatus.OK_STATUS;
			}
		};
		final SLJob top = new SLJob() {

			public String getName() {
				return "top";
			}

			public SLStatus run(SLProgressMonitor monitor) {
				monitor.begin(100);
				AbstractSLJob.invoke(sub, monitor, 50);

				SLProgressMonitor sub = new SubSLProgressMonitor(monitor,
						"local-sub-ind", 25);
				sub.begin();
				sub.done();

				sub = new SubSLProgressMonitor(monitor, "local-sub-10", 25);
				sub.begin(10);
				sub.worked(10);
				sub.done();

				monitor.done();
				return SLStatus.OK_STATUS;
			}
		};
		cj.submitJob(top);
		cj.shutdown();
	}
}
