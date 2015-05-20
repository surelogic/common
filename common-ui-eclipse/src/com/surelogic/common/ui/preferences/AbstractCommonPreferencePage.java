package com.surelogic.common.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.common.core.preferences.AutoPerspectiveSwitchPreferences;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;

/**
 * For apps that provide similar functionality, but use separate preference
 * settings (see IAutoPerspectiveSwitchPreferences)
 */
public abstract class AbstractCommonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  protected final String messagePrefix;
  private final AutoPerspectiveSwitchPreferences prefsBase;
  private BooleanFieldEditor f_promptPerspectiveSwitch;
  private BooleanFieldEditor f_autoPerspectiveSwitch;

  protected AbstractCommonPreferencePage(String prefix, AutoPerspectiveSwitchPreferences p) {
    messagePrefix = prefix;
    prefsBase = p;
  }

  @Override
  public void init(IWorkbench workbench) {
    setPreferenceStore(EclipseUIUtility.getPreferences());
    setDescription(I18N.msg(messagePrefix + "preference.page.title.msg"));
  }

  protected void setupForPerspectiveSwitch(final Group group) {
    f_promptPerspectiveSwitch = new BooleanFieldEditor(prefsBase.getPromptPerspectiveSwitchConstant(), I18N.msg(messagePrefix
        + "preference.page.promptPerspectiveSwitch"), group);
    f_promptPerspectiveSwitch.fillIntoGrid(group, 2);
    f_promptPerspectiveSwitch.setPage(this);
    f_promptPerspectiveSwitch.setPreferenceStore(EclipseUIUtility.getPreferences());
    f_promptPerspectiveSwitch.load();

    f_autoPerspectiveSwitch = new BooleanFieldEditor(prefsBase.getAutoPerspectiveSwitchConstant(), I18N.msg(messagePrefix
        + "preference.page.autoPerspectiveSwitch"), group);
    f_autoPerspectiveSwitch.fillIntoGrid(group, 2);
    f_autoPerspectiveSwitch.setPage(this);
    f_autoPerspectiveSwitch.setPreferenceStore(EclipseUIUtility.getPreferences());
    f_autoPerspectiveSwitch.load();
  }

  @Override
  protected void performDefaults() {
    f_promptPerspectiveSwitch.loadDefault();
    f_autoPerspectiveSwitch.loadDefault();
    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    f_promptPerspectiveSwitch.store();
    f_autoPerspectiveSwitch.store();
    return super.performOk();
  }
}
