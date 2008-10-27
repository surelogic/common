package com.surelogic.common.jobs;

import com.surelogic.common.i18n.I18N;

/**
 * A IDE independent sub-task of any {@link SLProgressMonitor}. It can be used
 * as follows:
 * 
 * <pre>
 * try {
 *   pm.begin(100);
 *   pm.worked(30);
 *   SubSLProgressMonitor subMonitor = new SubSLProgressMonitor(pm, &quot;sub 1&quot;, 40);
 *   try {
 *     subMonitor.begin(300);
 *     subMonitor.worked(100);
 *   } finally {
 *     subMonitor.done();
 *   }
 *   pm.worked(30)
 * } finally {
 *   pm.done();
 * }
 * </pre>
 * 
 * Cancellation is passed up to the parent. It is important to always call
 * {@link #done()} to ensure that the correct amount of work is done on the
 * parent progress monitor.
 * <p>
 * It is allowed to nest subtasks, but this is typically bad practice and may
 * not be well displayed in the UI. Hence, it is best to have one main task with
 * subtasks (i.e., not have subtasks of a subtask). This is allowed so that a
 * task that is normally invoked as a main task could be used as a subtask of
 * some higher task.
 */
public final class SubSLProgressMonitor implements SLProgressMonitor {

	/**
	 * Creates a new sub progress monitor that will do the specified amount of
	 * work on the parent monitor.
	 * 
	 * @param parent
	 *            the non-null parent progress monitor.
	 * @param name
	 *            the non-null name of this subtask.
	 * @param work
	 *            the amount of work that will be done on the parent progress
	 *            monitor.
	 */
	public SubSLProgressMonitor(SLProgressMonitor parent, String name, int work) {
		if (parent == null)
			throw new IllegalArgumentException(I18N.err(44, "parent"));
		f_parent = parent;
		if (name == null)
			throw new IllegalArgumentException(I18N.err(44, "name"));
		f_name = name;
		f_parentWorkedGoal = work;
	}

	private final String f_name;
	private final SLProgressMonitor f_parent;

	private final int f_parentWorkedGoal;
	private int f_parentWorked = 0;

	private int f_workedGoal;
	private int f_worked;

	public String getName() {
		return f_name;
	}

	public void begin() {
		/*
		 * We handle indeterminate subtasks by making them one unit of work.
		 * This unit will be ticked off when done is called.
		 */
		begin(1);
	}

	public void begin(int totalWork) {
		f_parent.subTask(f_name);
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
		f_parent.subTaskDone();
	}

	public boolean isCanceled() {
		return f_parent.isCanceled();
	}

	public void setCanceled(boolean value) {
		f_parent.setCanceled(value);
	}

	public void subTask(String name) {
		f_parent.subTask(name);
	}

	public void subTaskDone() {
		f_parent.subTaskDone();
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
