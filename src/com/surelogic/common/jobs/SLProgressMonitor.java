package com.surelogic.common.jobs;

/**
 * An IDE independent interface for job progress monitoring. Similar to the
 * Eclipse {@code IProgressMonitor} interface.
 * 
 * The {@link SLProgressMonitor} interface is implemented by objects that
 * monitor the progress of an activity; the methods in this interface are
 * invoked by code that performs the activity.
 * <p>
 * All activity is broken down into a linear sequence of tasks against which
 * progress is reported. When a task begins, a {@link #begin(int)} notification
 * is reported (or a {@link #begin()} if the task is indeterminate), followed by
 * any number and mixture of progress reports via {@link #worked(int)} and
 * subtask notifications via {@link SLProgressMonitor#subTask(String)}. When the
 * task is eventually completed, a {@link #done()} notification is reported.
 * After the {@link #done()} notification, the progress monitor cannot be
 * reused; i.e., {@link #begin(int)} cannot be called again after the call to
 * {@link #done()}.
 * <p>
 * A request to cancel an operation can be signaled using the
 * {@link #setCanceled(boolean)} method. Operations taking a progress monitor
 * are expected to poll the monitor (using {@link #isCanceled()}) periodically
 * and abort at their earliest convenience. Operation can however choose to
 * ignore cancellation requests.
 * <p>
 * Clients may implement this interface.
 */
public interface SLProgressMonitor {

	/**
	 * Start the progress indication for an indeterminate task. This must only
	 * be called once on a given progress monitor instance.
	 */
	public void begin();

	/**
	 * Start the progress indication for a task with known number of steps. This
	 * must only be called once on a given progress monitor instance.
	 * 
	 * @param totalWork
	 *            the total number of work units into which this task is
	 *            subdivided.
	 */
	public void begin(int totalWork);

	/**
	 * Notifies that the work is done; that is, either the main task is
	 * completed or the user canceled it. This method may be called more than
	 * once (implementations should be prepared to handle this case).
	 */
	public void done();

	/**
	 * Returns whether cancellation of current operation has been requested.
	 * Long-running operations should poll to see if cancellation has been
	 * requested.
	 * 
	 * @return <code>true</code> if cancellation has been requested, and
	 *         <code>false</code> otherwise
	 * @see #setCanceled(boolean)
	 */
	public boolean isCanceled();

	/**
	 * Sets the cancel state to the given value.
	 * 
	 * @param value
	 *            <code>true</code> indicates that cancellation has been
	 *            requested (but not necessarily acknowledged);
	 *            <code>false</code> clears this flag
	 * @see #isCanceled()
	 */
	public void setCanceled(boolean value);

	/**
	 * Notifies that a subtask of the main task is beginning. Subtasks are
	 * optional; the main task might not have subtasks.
	 * 
	 * @param name
	 *            the name (or description) of the subtask
	 */
	public void subTask(String name);

	/**
	 * Notifies that a given number of work unit of the main task has been
	 * completed. Note that this amount represents an installment, as opposed to
	 * a cumulative amount of work done to date.
	 * 
	 * @param work
	 *            a non-negative number of work units just completed
	 */
	public void worked(int work);
}
