package com.surelogic.common.core.preferences;

import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.common.core.EclipseUtility;

public final class CommonCorePreferencesUtility {

	private static final String PREFIX = "com.surelogic.common.core.";

	private static final AtomicBoolean f_initializationNeeded = new AtomicBoolean(
			true);

	public static void initializeDefaultScope() {
		if (f_initializationNeeded.compareAndSet(true, false)) {
			EclipseUtility.setDefaultBooleanPreference(WARN_LOW_MEMORY, true);
			/*
			 * We'll take the default-default for the other preferences.
			 */
		}
	}

	private static final String WARN_LOW_MEMORY = PREFIX + "warnLowMemory";
	private static final String SERVICEABILITY_EMAIL = PREFIX
			+ "serviceability.email";
	private static final String SERVICEABILITY_NAME = PREFIX
			+ "serviceability.name";

	public static boolean warnAboutLowMaximumMemory() {
		return EclipseUtility.getBooleanPreference(WARN_LOW_MEMORY);
	}

	public static void setWarnAboutLowMaximumMemory(boolean value) {
		EclipseUtility.setBooleanPreference(WARN_LOW_MEMORY, value);
	}

	public static String getServicabilityEmail() {
		return EclipseUtility.getStringPreference(SERVICEABILITY_EMAIL);
	}

	public static void setServicabilityEmail(String value) {
		EclipseUtility.setStringPreference(SERVICEABILITY_EMAIL, value);
	}

	public static String getServicabilityName() {
		return EclipseUtility.getStringPreference(SERVICEABILITY_NAME);
	}

	public static void setServicabilityName(String value) {
		EclipseUtility.setStringPreference(SERVICEABILITY_NAME, value);
	}

	private CommonCorePreferencesUtility() {
		// utility
	}
}
