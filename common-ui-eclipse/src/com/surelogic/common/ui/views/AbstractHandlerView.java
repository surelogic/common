package com.surelogic.common.ui.views;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.logging.SLLogger;

/**
 * An abstract view that can be a handler for commands. Subtypes should override
 * {@link #execute(ExecutionEvent)} and check the <tt>id</tt> of the event if
 * multiple events use this view as a handler. For example,
 * 
 * <pre>
 * public Object execute(ExecutionEvent event) throws ExecutionException {
 * 	final String id = event.getCommand().getId();
 * 	if (&quot;event1.id&quot;.equals(id)) {
 * 		// handle event1.id
 * 	} else if (&quot;event2.id&quot;.equals(id)) {
 * 		// handle event2.id
 * 	}
 * 	return null;
 * }
 * </pre>
 */
public abstract class AbstractHandlerView extends ViewPart implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// ignore
	}

	/**
	 * Implementations should override to handle commands sent to this view. For
	 * example,
	 * 
	 * <pre>
	 * public Object execute(ExecutionEvent event) throws ExecutionException {
	 * 	final String id = event.getCommand().getId();
	 * 	if (&quot;event1.id&quot;.equals(id)) {
	 * 		// handle event1.id
	 * 	} else if (&quot;event2.id&quot;.equals(id)) {
	 * 		// handle event2.id
	 * 	}
	 * 	return null;
	 * }
	 * </pre>
	 * 
	 * The default implementation logs a warning that this method was invoked
	 * but not overridden.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String id = event == null ? "null" : event.getCommand().getId();
		SLLogger.getLogger().warning("execute(\"" + id + "\") called but not handled...did you forget to override execute(ExecutionEvent)?");
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// ignore
	}
}
