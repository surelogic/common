package com.surelogic.common.core.preferences;

import com.surelogic.common.core.EclipseUtility;

/**
 * For plug-ins that provide similar functionality with regard to automatically
 * switching to their perspective but prompting the user (with a dialog) if this
 * perspective switch is okay, but use separate preference settings.
 */
public abstract class AutoPerspectiveSwitchPreferences {
	/**
	 * Boolean preference suffix that indicates if the user should be prompted
	 * by a dialog to switch to another perspective.
	 */
	private static final String PROMPT_PERSPECTIVE_SWITCH = "perspective.switch.prompt";

	public final String getPromptPerspectiveSwitchConstant() {
		return getConstant(PROMPT_PERSPECTIVE_SWITCH);
	}

	public final boolean getPromptPerspectiveSwitch() {
		return EclipseUtility
				.getBooleanPreference(getPromptPerspectiveSwitchConstant());
	}

	/**
	 * boolean preference suffix that indicates that the perspective switch
	 * should be performed automatically.
	 */
	private static final String AUTO_PERSPECTIVE_SWITCH = "perspective.switch.auto";

	public final String getAutoPerspectiveSwitchConstant() {
		return getConstant(AUTO_PERSPECTIVE_SWITCH);
	}

	public final boolean getAutoPerspectiveSwitch() {
		return EclipseUtility
				.getBooleanPreference(getAutoPerspectiveSwitchConstant());
	}

	/**
	 * Transforms a key suffix (typically {@link #PROMPT_PERSPECTIVE_SWITCH} or
	 * {@link #AUTO_PERSPECTIVE_SWITCH}) by adding the correct prefix value and
	 * returning a complete preference key.
	 * <p>
	 * The returned preference key can be used in the SureLogic preference API
	 * (see {@link EclipseUtility#PREFERENCES_NODE})
	 * 
	 * @param suffix
	 *            the suffix of a preference key.
	 * @return a complete preference key.
	 */
	protected abstract String getConstant(String suffix);
}
