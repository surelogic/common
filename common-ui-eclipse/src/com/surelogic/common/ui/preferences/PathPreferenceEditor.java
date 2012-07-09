package com.surelogic.common.ui.preferences;

import org.eclipse.swt.widgets.Label;

import com.surelogic.NotThreadSafe;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;

/**
 * 
 * Used to edit the JSure library annotation XML directory, but not specific to
 * that task.
 * <p>
 * Should be thread confined to the SWT thread.
 */
@NotThreadSafe
public final class PathPreferenceEditor {

	private final Label f_pathLabel;
	private final String f_prefKey;
	private final String f_origValue;
	private String f_value = null;

	public PathPreferenceEditor(final Label pathLabel, final String prefKey) {
		if (pathLabel == null)
			throw new IllegalStateException(I18N.err(44, "pathLabel"));
		f_pathLabel = pathLabel;

		if (prefKey == null)
			throw new IllegalStateException(I18N.err(44, "prefKey"));
		f_prefKey = prefKey;

		f_origValue = EclipseUtility.getStringPreference(prefKey);
		if (f_origValue == null)
			throw new IllegalStateException(I18N.err(253, f_prefKey));
	}

	public void store() {
		if ((f_value != null) && (!f_origValue.equals(f_value))) {
			EclipseUtility.setStringPreference(f_prefKey, f_value);
		}
	}

	public void loadDefault() {
		f_value = EclipseUtility.getDefaultStringPreference(f_prefKey);
		EclipseUtility.setStringPreference(f_prefKey, f_value);
		show();
	}

	public void set(String value) {
		f_value = value;
	}

	public void show() {
		f_pathLabel.setText(f_value == null ? f_origValue : f_value);
	}
}
