package com.surelogic.common.core.preferences;

import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.common.core.EclipseUtility;

public final class CommonCorePreferencesUtility {

  private static final String PREFIX = "com.surelogic.common.core.";

  private static final AtomicBoolean f_initializationNeeded = new AtomicBoolean(true);

  public static void initializeDefaultScope() {
    if (f_initializationNeeded.compareAndSet(true, false)) {
      EclipseUtility.setDefaultBooleanPreference(WARN_LOW_MEMORY, true);

      EclipseUtility.setDefaultIntPreference(QEDITOR_SASH_LHS_WEIGHT, 40);
      EclipseUtility.setDefaultIntPreference(QEDITOR_SASH_RHS_WEIGHT, 60);
      /*
       * We'll take the default-default for the other preferences.
       */
    }
  }

  public static final String QEDITOR_SASH_LHS_WEIGHT = PREFIX + "qeditor.sash.lhs.weight";
  public static final String QEDITOR_SASH_RHS_WEIGHT = PREFIX + "qeditor.sash.rhs.weight";
  public static final String QEDITOR_LHS_TAB_SELECTION = PREFIX + "qeditor.lhs.tab.selection";
  public static final String QEDITOR_SQL_TAB_SELECTION = PREFIX + "qeditor.sql.tab.selection";
  public static final String QEDITOR_FILTER_TREE_CHECK = PREFIX + "qeditor.filter.tree.check";

  private static final String WARN_LOW_MEMORY = PREFIX + "warnLowMemory";
  private static final String SERVICEABILITY_EMAIL = PREFIX + "serviceability.email";
  private static final String SERVICEABILITY_NAME = PREFIX + "serviceability.name";

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
