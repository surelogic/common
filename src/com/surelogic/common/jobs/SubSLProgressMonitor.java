package com.surelogic.common.jobs;

import com.surelogic.common.i18n.I18N;

/**
 * A IDE independent sub-task of any {@link SLProgressMonitor}. It can be used
 * as follows:
 * 
 * <pre>
 * try {
 * 	pm.beginTask(&quot;Main Task&quot;, 100);
 * 	doSomeWork(pm, 30);
 * 	SubSLProgressMonitor subMonitor = new SubSLProgressMonitor(pm, 40);
 * 	try {
 * 		subMonitor.beginTask(&quot;&quot;, 300);
 * 		doSomeWork(subMonitor, 300);
 * 	} finally {
 * 		subMonitor.done();
 * 	}
 * 	doSomeWork(pm, 30);
 * } finally {
 * 	pm.done();
 * }
 * </pre>
 * 
 * Cancellation is passed up to the parent. It is important to always call
 * {@link #done()} to ensure that the correct amount of work is done on the
 * parent progress monitor.
 * <p>
 * Calls to {@link #subTask(String)} are ignored.
 */
public final class SubSLProgressMonitor implements SLProgressMonitor {

	/**
	 * Creates a new sub progress monitor that will do the specified amount of
	 * work on the parent monitor.
	 * 
	 * @param parent
	 *            the parent progress monitor.
	 * @param work
	 *            the amount of work that will be done on the parent progress
	 *            monitor.
	 */
	public SubSLProgressMonitor(SLProgressMonitor parent, int work) {
		if (parent == null)
			throw new IllegalArgumentException(I18N.err(44, "parent"));
		f_parent = parent;
		f_parentWorkedGoal = work;
	}

	private final SLProgressMonitor f_parent;

	private final int f_parentWorkedGoal;
	private int f_parentWorked = 0;

	private int f_workedGoal;
	private int f_worked;

	public void beginTask(String name, int totalWork) {
		f_parent.subTask(name);
		if (totalWork <= 0)
			throw new IllegalStateException(I18N.err(115, "totalWork"));
		f_workedGoal = totalWork;
		f_worked = 0;
	}

	public void done() {
		final int parentWorkRemaining = f_parentWorkedGoal - f_parentWorked;
		if (parentWorkRemaining > 0) {
			f_parent.worked(parentWorkRemaining);
		}
		f_parent.subTask("");
	}

	public boolean isCanceled() {
		return f_parent.isCanceled();
	}

	public void setCanceled(boolean value) {
		f_parent.setCanceled(value);
	}

	public void subTask(String name) {
		// ignore
	}

	public void worked(int work) {
		if (f_worked < f_workedGoal) {
			f_worked = Math.min(f_worked + work, f_workedGoal);

			int goal = Math.round((float) f_worked * (float) f_parentWorkedGoal
					/ (float) f_workedGoal);
			if (f_parentWorked < goal) {
				int ticks = Math.min(goal - f_parentWorked, f_parentWorkedGoal
						- f_parentWorked);
				if (ticks > 0) {
					f_parent.worked(ticks);
					f_parentWorked += ticks;
				}
			}
		}
	}
}
