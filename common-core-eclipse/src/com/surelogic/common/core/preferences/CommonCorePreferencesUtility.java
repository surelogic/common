package com.surelogic.common.core.preferences;

import java.util.concurrent.atomic.AtomicBoolean;

import com.surelogic.common.core.EclipseUtility;

public final class CommonCorePreferencesUtility {

  private static final String PREFIX = "com.surelogic.common.core.";

  private static final AtomicBoolean f_initializationNeeded = new AtomicBoolean(true);

  public static void initializeDefaultScope() {
    if (f_initializationNeeded.compareAndSet(true, false)) {

      EclipseUtility.setDefaultIntPreference(QEDITOR_SASH_LHS_WEIGHT, 40);
      EclipseUtility.setDefaultIntPreference(QEDITOR_SASH_RHS_WEIGHT, 60);

      EclipseUtility.setDefaultIntPreference(QCEDITOR_SASH_LHS_WEIGHT, 40);
      EclipseUtility.setDefaultIntPreference(QCEDITOR_SASH_RHS_WEIGHT, 60);
      
      //EclipseUtility.setDefaultStringPreference(SERVICEABILITY_NAME, System.getProperty("user.name"));
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

  public static final String QCEDITOR_SASH_LHS_WEIGHT = PREFIX + "qceditor.sash.lhs.weight";
  public static final String QCEDITOR_SASH_RHS_WEIGHT = PREFIX + "qceditor.sash.rhs.weight";

  public static final String QMENU_SHOW_EMPTY_QUERIES = PREFIX + "qmenu.show.empty.queries";
  public static final String QMENU_SHOW_UNRUNNABLE_QUERIES = PREFIX + "qmenu.show.unrunnable.queries";

  private static final String SERVICEABILITY_EMAIL = PREFIX + "serviceability.email";
  private static final String SERVICEABILITY_NAME = PREFIX + "serviceability.name";

  public static String getServicabilityEmail() {
    return EclipseUtility.getStringPreference(SERVICEABILITY_EMAIL);
  }

  public static void setServicabilityEmail(String value) {
    EclipseUtility.setStringPreference(SERVICEABILITY_EMAIL, value);
  }

  public static String getServicabilityName() {
    String rv = EclipseUtility.getStringPreference(SERVICEABILITY_NAME);
    return rv;
  }

  public static void setServicabilityName(String value) {
    EclipseUtility.setStringPreference(SERVICEABILITY_NAME, value);
  }

  private CommonCorePreferencesUtility() {
    // utility
  }
}
