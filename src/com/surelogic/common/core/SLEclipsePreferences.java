package com.surelogic.common.core;

import java.util.logging.Level;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This class manages Eclipse {@link IEclipsePreferences} for the SureLogic
 * Eclipse-based tools.
 * <p>
 * This class is not public and the Eclipse {@link IEclipsePreferences} it
 * manages should be obtained via the {@link EclipseUtility#getPreferences()}
 * method.
 */
final class SLEclipsePreferences {

	private static final SLEclipsePreferences INSTANCE = new SLEclipsePreferences();

	static SLEclipsePreferences getInstance() {
		return INSTANCE;
	}

	private SLEclipsePreferences() {
		// singleton
	}

	private IEclipsePreferences f_preferences;

	/**
	 * Called the plug-in {@link Activator}.
	 */
	void init() {
		final boolean isNull;
		synchronized (this) {
			f_preferences = new InstanceScope()
					.getNode(EclipseUtility.PREFERENCES_NODE);
			isNull = f_preferences == null;
		}
		if (isNull) {
			final String msg = I18N.err(218, EclipseUtility.PREFERENCES_NODE);
			SLLogger.getLogger().log(Level.SEVERE, msg);
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Gets the SureLogic Eclipse preferences. Client code, other than
	 * {@link Activator} and {@link EclipseUtility}, should use
	 * {@link EclipseUtility#getPreferences()} rather than calling this method
	 * directly
	 * 
	 * @return the SureLogic Eclipse preferences.
	 * @throws IllegalStateException
	 *             if the SureLogic Eclipse preferences are not loaded. This
	 *             would indicate a bug.
	 * 
	 * @see EclipseUtility#getPreferences()
	 */
	IEclipsePreferences getPreferences() {
		final IEclipsePreferences result;
		synchronized (this) {
			result = f_preferences;
		}
		if (result == null)
			throw new IllegalStateException(I18N.err(220,
					EclipseUtility.PREFERENCES_NODE));
		return result;
	}

	/**
	 * Called the plug-in {@link Activator}.
	 */
	void dispose() {
		final IEclipsePreferences result;
		synchronized (this) {
			result = f_preferences;
			f_preferences = null;
		}
		try {
			result.flush();
		} catch (BackingStoreException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(219, EclipseUtility.PREFERENCES_NODE), e);
		}
	}
}
