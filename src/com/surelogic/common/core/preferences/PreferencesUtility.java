package com.surelogic.common.core.preferences;

import com.surelogic.common.core.EclipseUtility;

public final class PreferencesUtility {

	private static final String PREFIX = "com.surelogic.common.core.";

	public static final String P_WARN_LOW_MEMORY = PREFIX + "warnLowMemory";

	public static boolean warnAboutLowMaximumMemory() {
		return EclipseUtility.getPreferences().getBoolean(P_WARN_LOW_MEMORY,
				true);
	}

	public static void setWarnAboutLowMaximumMemory(boolean value) {
		EclipseUtility.getPreferences().putBoolean(P_WARN_LOW_MEMORY, value);
	}

	public static final String P_SERVICEABILITY_EMAIL = PREFIX
			+ "serviceability.email";

	public static String getServicabilityEmail() {
		return EclipseUtility.getPreferences().get(P_SERVICEABILITY_EMAIL, "");
	}

	public static void setServicabilityEmail(String value) {
		if (value == null)
			value = "";
		EclipseUtility.getPreferences().put(P_SERVICEABILITY_EMAIL, value);
	}

	public static final String P_SERVICEABILITY_NAME = PREFIX
			+ "serviceability.name";

	public static String getServicabilityName() {
		return EclipseUtility.getPreferences().get(P_SERVICEABILITY_NAME, "");
	}

	public static void setServicabilityName(String value) {
		if (value == null)
			value = "";
		EclipseUtility.getPreferences().put(P_SERVICEABILITY_NAME, value);
	}

	private PreferencesUtility() {
		// utility
	}
}
