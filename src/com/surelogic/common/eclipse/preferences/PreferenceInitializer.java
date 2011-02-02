package com.surelogic.common.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.surelogic.common.eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_WARN_LOW_MAXIMUM_MEMORY, true);
		store.setDefault(PreferenceConstants.P_SERVICEABILITY_EMAIL, "");
		store.setDefault(PreferenceConstants.P_SERVICEABILITY_NAME, "");
	}
}
