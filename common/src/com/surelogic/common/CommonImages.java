package com.surelogic.common;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.surelogic.Utility;

/**
 * A utility that defines all the images used by SureLogic code as well as some
 * utility methods to obtain references to the files on the filesystem.
 */
@Utility
public final class CommonImages {
  private CommonImages() {
    // To prevent instantiation
  }

  public static final String PATH = "/lib/images/";

  /*
   * Symbolic decorator names
   */
  public static final String DECR_ABSTRACT = "decr/abstract.gif";
  public static final String DECR_ANDROID = "decr/android.png";
  public static final String DECR_ANNOTATION = "decr/annotation.gif";
  public static final String DECR_ASSUME = "decr/assume.gif";
  public static final String DECR_ASTERISK = "decr/asterisk.gif";
  public static final String DECR_BUGLINK = "decr/buglink.gif";
  public static final String DECR_CONSISTENT = "decr/consistent.gif";
  public static final String DECR_CONSTRUCTOR = "decr/constructor.gif";
  public static final String DECR_DELTA = "decr/delta.gif";
  public static final String DECR_ERROR = "decr/error.gif";
  public static final String DECR_FINAL = "decr/final.gif";
  public static final String DECR_FLASHLIGHT = "decr/flashlight.gif";
  public static final String DECR_IMPLICIT = "decr/implicit.gif";
  public static final String DECR_INCONSISTENT = "decr/inconsistent.gif";
  public static final String DECR_INFO = "decr/info.gif";
  public static final String DECR_JAVA = "decr/java.gif";
  public static final String DECR_NEW = "decr/new.gif";
  public static final String DECR_RECURSIVE = "decr/recursive.gif";
  public static final String DECR_REDDOT = "decr/reddot.gif";
  public static final String DECR_STATIC_ABSTRACT = "decr/static_abstract.gif";
  public static final String DECR_STATIC_FINAL = "decr/static_final.gif";
  public static final String DECR_STATIC_VOLATILE = "decr/static_volatile.gif";
  public static final String DECR_STATIC = "decr/static.gif";
  public static final String DECR_TEAM_SERVER = "decr/team_server.gif";
  public static final String DECR_TRUSTED = "decr/trusted.gif";
  public static final String DECR_UNUSED_CONSISTENT = "decr/unused_consistent.gif";
  public static final String DECR_UNUSED_INCONSISTENT = "decr/unused_inconsistent.gif";
  public static final String DECR_UNUSED = "decr/unused.gif";
  public static final String DECR_VIRTUAL = "decr/virtual.gif";
  public static final String DECR_VOLATILE = "decr/volatile.gif";
  public static final String DECR_WARNING = "decr/warning.gif";

  /*
   * Symbolic image names
   */
  public static final String IMG_ALPHA_SORT = "alpha_sort.gif";
  public static final String IMG_ANALYSIS_RESULT = "analysis_result.gif";
  public static final String IMG_ANDROID_APP = "android_app.gif";
  public static final String IMG_ANNOTATION_ABDUCTIVE = "annotation_abductive.gif";
  public static final String IMG_ANNOTATION_DEFAULT = "annotation_default.gif";
  public static final String IMG_ANNOTATION_DELTA = "annotation_delta.gif";
  public static final String IMG_ANNOTATION_PRIVATE = "annotation_private.gif";
  public static final String IMG_ANNOTATION_PROPOSED = "annotation_proposed.gif";
  public static final String IMG_ANNOTATION_PROTECTED = "annotation_protected.gif";
  public static final String IMG_ANNOTATION = "annotation.gif";
  public static final String IMG_ARROW_DOWN = "arrow_down.gif";
  public static final String IMG_ARROW_RIGHT = "arrow_right.gif";
  public static final String IMG_ASTERISK_DIAMOND_GRAY = "asterisk_diamond_gray.gif";
  public static final String IMG_ASTERISK_DIAMOND_ORANGE = "asterisk_diamond_orange.gif";
  public static final String IMG_ASTERISK_GRAY = "asterisk_gray.gif";
  public static final String IMG_ASTERISK_ORANGE_0 = "asterisk_orange_0.gif";
  public static final String IMG_ASTERISK_ORANGE_100 = "asterisk_orange_100.gif";
  public static final String IMG_ASTERISK_ORANGE_25 = "asterisk_orange_25.gif";
  public static final String IMG_ASTERISK_ORANGE_50 = "asterisk_orange_50.gif";
  public static final String IMG_ASTERISK_ORANGE_75 = "asterisk_orange_75.gif";
  public static final String IMG_CATEGORY = "category.gif";
  public static final String IMG_CHANGELOG_OLD_SCAN_ONLY = "changelog_old_scan_only.gif";
  public static final String IMG_CHANGELOG_ONLY = "changelog_only.gif";
  public static final String IMG_CHANGELOG = "changelog.gif";
  public static final String IMG_CHOICE_ITEM = "choice_item.gif";
  public static final String IMG_CHOICE = "choice.gif";
  public static final String IMG_CIRCLE_GRAY = "circle_gray.png";
  public static final String IMG_CIRCLE_GREEN = "circle_green.png";
  public static final String IMG_CIRCLE_RED = "circle_red.png";
  public static final String IMG_CIRCLE_YELLOW = "circle_yellow.png";
  public static final String IMG_CLASS_DEFAULT = "class_default.gif";
  public static final String IMG_CLASS_PRIVATE = "class_private.gif";
  public static final String IMG_CLASS_PROTECTED = "class_protected.gif";
  public static final String IMG_CLASS = "class.gif";
  public static final String IMG_COLLAPSE_ALL = "collapse_all.gif";
  public static final String IMG_COLUMNS = "columns.gif";
  public static final String IMG_COMMENT = "comment.gif";
  public static final String IMG_CONSOLE = "console.gif";
  public static final String IMG_CPU = "cpu.gif";
  public static final String IMG_CPU_SUBDUED = "cpu_subdued.gif";
  public static final String IMG_DOC = "doc.gif";
  public static final String IMG_DOWN = "down.gif";
  public static final String IMG_DRUM_EXPLORER = "drum_explorer.gif";
  public static final String IMG_DRUM_GRAY = "drum_gray.gif";
  public static final String IMG_DRUM_U = "drum_u.gif";
  public static final String IMG_DRUM = "drum.gif";
  public static final String IMG_EDIT_ADD = "edit_add.gif";
  public static final String IMG_EDIT_COPY = "edit_copy.gif";
  public static final String IMG_EDIT_CUT = "edit_cut.gif";
  public static final String IMG_EDIT_DELETE = "edit_delete.gif";
  public static final String IMG_EDIT_NEW = "edit_new.gif";
  public static final String IMG_EDIT_PASTE = "edit_paste.gif";
  public static final String IMG_EDIT_PRINT = "edit_print.gif";
  public static final String IMG_EDIT_REDO = "edit_redo.gif";
  public static final String IMG_EDIT_UNDO = "edit_undo.gif";
  public static final String IMG_EMPTY_DOT = "empty_dot.gif";
  public static final String IMG_EMPTY = "empty.gif";
  public static final String IMG_ENUM_DEFAULT = "enum_default.gif";
  public static final String IMG_ENUM_PRIVATE = "enum_private.gif";
  public static final String IMG_ENUM_PROTECTED = "enum_protected.gif";
  public static final String IMG_ENUM = "enum.gif";
  public static final String IMG_ERROR = "error.gif";
  public static final String IMG_EXPAND_ALL = "expand_all.gif";
  public static final String IMG_EXPORT_WEB = "export_ws_wiz.png";
  public static final String IMG_EXPORT_WITH_SOURCE = "export_with_source.gif";
  public static final String IMG_EXPORT = "export.gif";
  public static final String IMG_FIELD_DEFAULT = "field_default.gif";
  public static final String IMG_FIELD_PRIVATE = "field_private.gif";
  public static final String IMG_FIELD_PROTECTED = "field_protected.gif";
  public static final String IMG_FIELD_PUBLIC = "field_public.gif";
  public static final String IMG_FILE = "file.gif";
  public static final String IMG_FILTER = "filter.gif";
  public static final String IMG_FINDBUGS_FINDING = "findbugs_finding.gif";
  public static final String IMG_FL_COMP_UNIT = "fl_comp_unit.gif";
  public static final String IMG_FL_LOGO = "fl_logo.gif";
  public static final String IMG_FL_PREP_DATA = "fl_prep_data.gif";
  public static final String IMG_FL_RUN_VIEW = "fl_run_view.gif";
  public static final String IMG_FL_RUN_CONTROL = "fl_run_control.gif";
  public static final String IMG_FLASHLIGHT_OVERVIEW_BANNER = "flashlight_overview_banner.png";
  public static final String IMG_FOLDER = "folder.gif";
  public static final String IMG_GRAY_X_DOT = "gray_x_dot.gif";
  public static final String IMG_GRAY_X_DOUBLE = "gray_x_double.gif";
  public static final String IMG_GRAY_X_LIGHT = "gray_x_light.gif";
  public static final String IMG_GRAY_X = "gray_x.gif";
  public static final String IMG_GREEN_DOT = "green_dot.gif";
  public static final String IMG_HAPPENS_BEFORE_NO = "happens_before_no.gif";
  public static final String IMG_HAPPENS_BEFORE = "happens_before.gif";
  public static final String IMG_HIERARCHICAL = "hierarchical.gif";
  public static final String IMG_IMPORT = "import.gif";
  public static final String IMG_INFO = "info.gif";
  public static final String IMG_INTERFACE_DEFAULT = "interface_default.gif";
  public static final String IMG_INTERFACE_PRIVATE = "interface_private.gif";
  public static final String IMG_INTERFACE_PROTECTED = "interface_protected.gif";
  public static final String IMG_INTERFACE = "interface.gif";
  public static final String IMG_JAR_SAVEAS = "jar_saveas.gif";
  public static final String IMG_JAR = "jar.gif";
  public static final String IMG_JAVA_APP = "java_app.gif";
  public static final String IMG_JAVA_CLASS_FILE = "java_class_file.gif";
  public static final String IMG_JAVA_COMP_UNIT = "java_comp_unit.gif";
  public static final String IMG_JAVA_DECLS_TREE = "java_decls_tree.gif";
  public static final String IMG_JAVA_SORT = "java_sort.gif";
  public static final String IMG_JETTY_LOG = "jetty_log.gif";
  public static final String IMG_JSECURE_LOGO = "jsecure_logo.png";
  public static final String IMG_JSURE_COMP_UNIT = "jsure_comp_unit.gif";
  public static final String IMG_JSURE_DRUM = "jsure_drum.gif";
  public static final String IMG_JSURE_EXPLORER = "jsure_explorer.gif";
  public static final String IMG_JSURE_FINDER_DOT = "jsure_finder_dot.gif";
  public static final String IMG_JSURE_FINDER = "jsure_finder.gif";
  public static final String IMG_JSURE_GRAPH = "jsure_graph.gif";
  public static final String IMG_JSURE_LOGO = "jsure_logo.gif";
  public static final String IMG_JSURE_MODEL_PROBLEMS_EXIST = "jsure_model_problems_exist.gif";
  public static final String IMG_JSURE_MODEL_PROBLEMS = "jsure_model_problems.gif";
  public static final String IMG_JSURE_QUICK_REF_ICON = "jsure_quick_ref_icon.gif";
  public static final String IMG_JSURE_QUICK_REF = "jsure_quick_ref.png";
  public static final String IMG_JSURE_RE_VERIFY = "jsure_re_verify.gif";
  public static final String IMG_JSURE_VERIFY = "jsure_verify.gif";
  public static final String IMG_LEFT_ARROW_SMALL = "left_arrow_small.gif";
  public static final String IMG_LEFT = "left.gif";
  public static final String IMG_LIBRARY = "library.gif";
  public static final String IMG_LICENSE_GRAY = "license_gray.gif";
  public static final String IMG_LICENSE_NO_SEAL = "license_no_seal.gif";
  public static final String IMG_LICENSE_NO_SEAL_GRAY = "license_no_seal_gray.gif";
  public static final String IMG_LICENSE = "license.gif";
  public static final String IMG_LIGHTBULB = "lightbulb.gif";
  public static final String IMG_LINK = "link.gif";
  public static final String IMG_LOCK = "lock.gif";
  public static final String IMG_METHOD_DEFAULT = "method_default.gif";
  public static final String IMG_METHOD_PRIVATE = "method_private.gif";
  public static final String IMG_METHOD_PROTECTED = "method_protected.gif";
  public static final String IMG_METHOD_PUBLIC = "method_public.gif";
  public static final String IMG_OPEN_XML_TYPE = "open_xml_type.gif";
  public static final String IMG_ONLINE_DOWN = "online_down.png";
  public static final String IMG_ONLINE_FILLER = "online_filler.png";
  public static final String IMG_ONLINE_RIGHT = "online_right.png";
  public static final String IMG_PACKAGE_BINARY = "package_binary.gif";
  public static final String IMG_PACKAGE = "package.gif";
  public static final String IMG_PAGE_BLUE = "page_blue.gif";
  public static final String IMG_PAGE_ORANGE = "page_orange.gif";
  public static final String IMG_PARAMETER = "parameter.gif";
  public static final String IMG_PLUS_VOUCH = "plus_vouch.gif";
  public static final String IMG_PLUS = "plus.gif";
  public static final String IMG_PMD_FINDING = "pmd_finding.gif";
  public static final String IMG_PREREQUISITE_GRAY = "prerequisite_gray.gif";
  public static final String IMG_PREREQUISITE = "prerequisite.gif";
  public static final String IMG_PRIORITY = "priority.gif";
  public static final String IMG_PROJECT = "project.gif";
  public static final String IMG_QUERY_BACK = "query_back.gif";
  public static final String IMG_QUERY_DEFAULT = "query_default.gif";
  public static final String IMG_QUERY_EMPTY = "query_empty.gif";
  public static final String IMG_QUERY_GRAY = "query_gray.gif";
  public static final String IMG_QUERY_OBJ_HIDDEN = "query_obj_hidden.gif";
  public static final String IMG_QUERY_OBJ_ROOT = "query_obj_root.gif";
  public static final String IMG_QUERY_OBJ = "query_obj.gif";
  public static final String IMG_QUERY = "query.gif";
  public static final String IMG_QUICK_ASSIST_DECR = "quick_assist_decr.gif";
  public static final String IMG_QUICK_ASSIST_FIX_ERROR = "quick_assist_fix_error.gif";
  public static final String IMG_QUICK_ASSIST_FIX_WARNING = "quick_assist_fix_warning.gif";
  public static final String IMG_QUICK_ASSIST = "quick_assist.gif";
  public static final String IMG_RED_X = "red_x.gif";
  public static final String IMG_REFRESH = "refresh.gif";
  public static final String IMG_RIGHT_ARROW_SMALL = "right_arrow_small.gif";
  public static final String IMG_RIGHT = "right.gif";
  public static final String IMG_RUN_DRUM = "run_drum.gif";
  public static final String IMG_RUN = "run.gif";
  public static final String IMG_SAVE_EDIT = "save_edit.gif";
  public static final String IMG_SAVEALL_EDIT = "saveall_edit.gif";
  public static final String IMG_SAVEAS_EDIT = "saveas_edit.gif";
  public static final String IMG_SIERRA_DISCONNECT = "sierra_disconnect.gif";
  public static final String IMG_SIERRA_INVESTIGATE_DOT = "sierra_investigate_dot.gif";
  public static final String IMG_SIERRA_INVESTIGATE = "sierra_investigate.gif";
  public static final String IMG_SIERRA_LOGO = "sierra_logo.gif";
  public static final String IMG_SIERRA_POWERED_BY_SURELOGIC_REALLY_SHORT = "sierra_powered_by_surelogic_really_short.png";
  public static final String IMG_SIERRA_POWERED_BY_SURELOGIC_SHORT = "sierra_powered_by_surelogic_short.png";
  public static final String IMG_SIERRA_POWERED_BY_SURELOGIC = "sierra_powered_by_surelogic.png";
  public static final String IMG_SIERRA_PUBLISH = "sierra_publish.gif";
  public static final String IMG_SIERRA_SCAN_CLASS = "sierra_scan_class.gif";
  public static final String IMG_SIERRA_SCAN_DELTA = "sierra_scan_delta.gif";
  public static final String IMG_SIERRA_SCAN_PKG = "sierra_scan_pkg.gif";
  public static final String IMG_SIERRA_SCAN = "sierra_scan.gif";
  public static final String IMG_SIERRA_SERVER_GRAY = "sierra_server_gray.gif";
  public static final String IMG_SIERRA_SERVER_LOCAL = "sierra_server_local.gif";
  public static final String IMG_SIERRA_SERVER = "sierra_server.gif";
  public static final String IMG_SIERRA_STAMP_SMALL = "sierra_stamp_small.gif";
  public static final String IMG_SIERRA_STAMP = "sierra_stamp.png";
  public static final String IMG_SIERRA_SYNC = "sierra_sync.gif";
  public static final String IMG_SORT_DOWN = "sort_down.gif";
  public static final String IMG_SORT_UP = "sort_up.gif";
  public static final String IMG_SUGGESTIONS_WARNINGS = "suggestions_warnings.gif";
  public static final String IMG_THREAD = "thread.gif";
  public static final String IMG_TIMEOUT_X = "timeout_x.gif";
  public static final String IMG_TRAFFIC_LIGHT_GREEN = "traffic_light_green.png";
  public static final String IMG_TRAFFIC_LIGHT_RED = "traffic_light_red.png";
  public static final String IMG_TRAFFIC_LIGHT_YELLOW = "traffic_light_yellow.png";
  public static final String IMG_TRANSPARENT_PIXEL = "transparent_pixel.png";
  public static final String IMG_TYPE_PARAMETER = "type_parameter.gif";
  public static final String IMG_UNKNOWN = "unknown.gif";
  public static final String IMG_UP = "up.gif";
  public static final String IMG_VERIFICATION_RESULT = "verification_result.gif";
  public static final String IMG_WARNING = "warning.gif";

  /**
   * Gets the URL to the passed name within this package.
   * 
   * @param imageSymbolicName
   *          the image's symbolic name.
   * @return a URL or {@code null} if no resource was found.
   */
  public static URL getImageURL(final String imageSymbolicName) {
    final String path = PATH + imageSymbolicName;
    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    return url;
  }

  /**
   * Creates a Swing {@link Icon} from the image file in this package associated
   * with the passed image symbolic name.
   * 
   * @param imageSymbolicName
   *          the image's symbolic name.
   * @return an icon or {@code null} if the image file could not be found.
   */
  public static Icon getIcon(final String imageSymbolicName) {
    final URL imgURL = getImageURL(imageSymbolicName);
    if (imgURL != null) {
      return new ImageIcon(imgURL, imageSymbolicName);
    }
    // nothing found
    return null;
  }

  /**
   * Creates a Swing {@link Image} from the image file in this package
   * associated with the passed image symbolic name.
   * 
   * @param imageSymbolicName
   *          the image's symbolic name.
   * @return an image or {@code null} if the image file could not be found.
   */
  public static Image getJavaImage(final String imageSymbolicName) {
    final URL imgURL = getImageURL(imageSymbolicName);
    if (imgURL != null) {
      return Toolkit.getDefaultToolkit().createImage(imgURL);
    }
    // nothing found
    return null;
  }
}
