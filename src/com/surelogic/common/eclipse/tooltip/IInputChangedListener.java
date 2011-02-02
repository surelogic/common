package com.surelogic.common.eclipse.tooltip;

/**
 * A listener which is notified when the target's input changes.
 * <p>
 * Clients can implement that interface and its extension interfaces.
 * </p>
 * 
 * @since 3.4
 */
public interface IInputChangedListener {

	/**
	 * Called when a the input has changed.
	 * 
	 * @param newInput
	 *            the new input, or <code>null</code> iff the listener should
	 *            not show any new input
	 */
	void inputChanged(Object newInput);
}
