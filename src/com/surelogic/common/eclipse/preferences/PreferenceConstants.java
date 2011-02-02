package com.surelogic.common.eclipse.preferences;

import com.surelogic.common.eclipse.Activator;

/**
 * Constant definitions for plug-in preferences with getters and setters.
 */
public class PreferenceConstants {

	private static final String PREFIX = "com.surelogic.common.eclipse.";

	public static final String P_WARN_LOW_MAXIMUM_MEMORY = PREFIX
			+ "warn-low-maximum-memory";

	public static boolean warnAboutLowMaximumMemory() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				P_WARN_LOW_MAXIMUM_MEMORY);
	}

	public static void setWarnAboutLowMaximumMemory(boolean value) {
		Activator.getDefault().getPreferenceStore().setValue(
				P_WARN_LOW_MAXIMUM_MEMORY, value);
	}

	public static final String P_SERVICEABILITY_EMAIL = PREFIX
			+ "serviceability-email";

	public static String getServicabilityEmail() {
		String val = Activator.getDefault().getPreferenceStore().getString(
				P_SERVICEABILITY_EMAIL);
		if (val != null)
			return val;
		else
			return "";
	}

	public static void setServicabilityEmail(String value) {
		if (value == null)
			value = "";
		Activator.getDefault().getPreferenceStore().setValue(
				P_SERVICEABILITY_EMAIL, value);
	}

	public static final String P_SERVICEABILITY_NAME = PREFIX
			+ "serviceability-name";

	public static String getServicabilityName() {
		String val = Activator.getDefault().getPreferenceStore().getString(
				P_SERVICEABILITY_NAME);
		if (val != null)
			return val;
		else
			return "";
	}

	public static void setServicabilityName(String value) {
		if (value == null)
			value = "";
		Activator.getDefault().getPreferenceStore().setValue(
				P_SERVICEABILITY_NAME, value);
	}
}
