package com.surelogic.common.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * For plug ins that provide similar functionality with regard to automatically
 * switching to their perspective but prompting the user if this is okay, but
 * use separate preference settings.
 */
public interface IAutoPerspectiveSwitchPreferences {
	IPreferenceStore getPreferenceStore();

	String getPrefConstant(String suffix);

	String PROMPT_PERSPECTIVE_SWITCH = "perspective.switch.prompt";

	boolean getPromptForPerspectiveSwitch();

	void setPromptForPerspectiveSwitch(boolean value);

	String AUTO_PERSPECTIVE_SWITCH = "perspective.switch.auto";

	boolean getAutoPerspectiveSwitch();

	void setAutoPerspectiveSwitch(boolean value);
}
