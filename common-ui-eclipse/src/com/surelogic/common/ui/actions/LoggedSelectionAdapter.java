package com.surelogic.common.ui.actions;

import java.util.logging.Level;

import org.eclipse.swt.events.*;

import com.surelogic.common.logging.SLLogger;

public class LoggedSelectionAdapter extends SelectionAdapter {
	final String context;
	
	public LoggedSelectionAdapter(String context) {
		this.context = context;
	}

	public String getContext() {
		return context;
	}
	
	@Override
	public final void widgetSelected(SelectionEvent e) {
		try {
			selected(e);
		} catch(RuntimeException x) {
			SLLogger.getLogger().log(Level.SEVERE, "Exception in "+context, x);
			throw x;
		}
	}

	@Override
	public final void widgetDefaultSelected(SelectionEvent e) {
		try {
			defaultSelected(e);
		} catch(RuntimeException x) {
			SLLogger.getLogger().log(Level.SEVERE, "Exception in "+context, x);
			throw x;
		}
	}
	
	protected void selected(SelectionEvent e) {
	}

	protected void defaultSelected(SelectionEvent e) {
	}
}
