package com.surelogic.common.eclipse.jobs;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;

/**
 * Adapts an Eclipse status progress monitor object to the IDE independent
 * {@link SLProgressMonitor} interface.
 */
public class SLProgressMonitorWrapper implements SLProgressMonitor {

	/**
	 * The wrapped progress monitor.
	 */
	protected final IProgressMonitor f_monitor;

	/**
	 * The name the job being monitored.
	 */
	private final String f_name;

	private boolean f_started = false;

	/**
	 * Stack of names of the tasks that contain the currently running sub task.
	 * Used to return the progress monitor name to the previous subtask when a
	 * subtask is finished. The name of the currently running subtask is found
	 * in {@link #f_currentSubTask}.
	 */
	private final LinkedList<String> f_enclosingSubTasks = new LinkedList<String>();

	/**
	 * The name of the currently running subtask, or {@code ""} if no subtask is
	 * running.
	 */
	private String f_currentSubTask = "";

	/**
	 * Creates a new wrapper around the given monitor.
	 * 
	 * @param monitor
	 *            the progress monitor to forward to.
	 * @param name
	 *            the task name.
	 */
	public SLProgressMonitorWrapper(IProgressMonitor monitor, String name) {
		if (monitor == null)
			throw new IllegalArgumentException(I18N.err(44, "monitor"));
		f_monitor = monitor;
		if (name == null)
			throw new IllegalArgumentException(I18N.err(44, "name"));
		f_name = name;

	}

	public void begin() {
		if (f_started) {
			throw new IllegalStateException(I18N.err(118));
		}
		f_started = true;
		f_monitor.beginTask(f_name, IProgressMonitor.UNKNOWN);
	}

	public void begin(int totalWork) {
		if (f_started) {
			throw new IllegalStateException(I18N.err(118));
		}
		f_started = true;
		f_monitor.beginTask(f_name, totalWork);
	}

	public void done() {
		while (!f_enclosingSubTasks.isEmpty()) {
			subTaskDone();
		}
		f_monitor.done();
	}

	public boolean isCanceled() {
		return f_monitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		f_monitor.setCanceled(value);
	}

	public void subTask(String name) {
		if (!f_started) {
			throw new IllegalStateException(I18N.err(119, "subTask"));
		}
		f_enclosingSubTasks.addFirst(f_currentSubTask);
		f_currentSubTask = name;
		f_monitor.subTask(name);
	}

	public void subTaskDone() {
		// restore the previous sub task name
		f_currentSubTask = f_enclosingSubTasks.removeFirst();
		f_monitor.subTask(f_currentSubTask);
	}

	public void worked(int work) {
		if (!f_started) {
			throw new IllegalStateException(I18N.err(119, "worked"));
		}
		f_monitor.worked(work);
	}
}
