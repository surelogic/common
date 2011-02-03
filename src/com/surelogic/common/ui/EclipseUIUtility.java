package com.surelogic.common.ui;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.surelogic.common.core.EclipseUtility;

public final class EclipseUIUtility {

	/**
	 * Gets the Eclipse {@link IPreferenceStore} for the SureLogic Eclipse-based
	 * tools. This is usable in the Eclipse UI, most notably in preference
	 * dialogs.
	 * <p>
	 * These are the same preferences returned by
	 * {@link EclipseUtility#getPreferences()}, however, the
	 * {@link IEclipsePreferences} returned by that method cannot be used in
	 * preference dialogs. It should, however, be used in all other code to
	 * avoid Eclipse UI dependencies.
	 * <p>
	 * These preferences are persisted within per-workspace.
	 * 
	 * @return the SureLogic Eclipse preferences usable in preference dialogs.
	 * 
	 * @see EclipseUtility#getPreferences()
	 * @see EclipseUtility#PREFERENCES_NODE
	 */
	public static IPreferenceStore getPreferences() {
		return new ScopedPreferenceStore(new InstanceScope(),
				EclipseUtility.PREFERENCES_NODE);
	}

	private EclipseUIUtility() {
		// utility
	}
}
