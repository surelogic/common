package com.surelogic.common.images;

import java.awt.Image;
import java.net.URL;

import com.surelogic.common.logging.SLLogger;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class CommonImages {
	public static final String PATH = "/com/surelogic/common/images/";

	/*
	 * Shared image keys:
	 */
	public static final String IMG_ANNOTATION = "annotation.gif";
	public static final String IMG_ASTERISK_DIAMOND_GRAY = "asterisk_diamond_gray.gif";
	public static final String IMG_ASTERISK_DIAMOND_ORANGE = "asterisk_diamond_orange.gif";
	public static final String IMG_ASTERISK_GRAY = "asterisk_gray.gif";
	public static final String IMG_ASTERISK_ORANGE_0 = "asterisk_orange_0.gif";
	public static final String IMG_ASTERISK_ORANGE_25 = "asterisk_orange_25.gif";
	public static final String IMG_ASTERISK_ORANGE_50 = "asterisk_orange_50.gif";
	public static final String IMG_ASTERISK_ORANGE_75 = "asterisk_orange_75.gif";
	public static final String IMG_ASTERISK_ORANGE_100 = "asterisk_orange_100.gif";
	public static final String IMG_BUGLINK_DECR = "buglink_decr.gif";
	public static final String IMG_CATEGORY = "category.gif";
	public static final String IMG_COLUMNS = "columns.gif";
	public static final String IMG_COMMENT = "comment.gif";
	public static final String IMG_CONSOLE = "console.gif";
	public static final String IMG_DOWN = "down.gif";
	public static final String IMG_EXPORT_WEB = "export_ws_wiz.png";
	public static final String IMG_EXPORT = "export.gif";
	public static final String IMG_FILTER = "filter.gif";
	public static final String IMG_FINDBUGS_FINDING = "findbugs_finding.gif";
	public static final String IMG_FL_APP = "fl_app.gif";
	public static final String IMG_FL_LOGO = "fl_logo.gif";
	public static final String IMG_FL_PREP_DATA = "fl_prep_data.gif";
	public static final String IMG_FL_PREP = "fl_prep.gif";
	public static final String IMG_FL_READ_LOG = "fl_read_log.gif";
	public static final String IMG_FL_RUN_VIEW = "fl_run_view.gif";
	public static final String IMG_FL_RUN = "fl_run.gif";
	public static final String IMG_GRAY_X_DOT = "gray_x_dot.gif";
	public static final String IMG_GRAY_X = "gray_x.gif";
	public static final String IMG_GRAY_X_DOUBLE = "grey_x_double.gif";
	public static final String IMG_IMPORT = "import.gif";
	public static final String IMG_JETTY_LOG = "jetty_log.gif";
	public static final String IMG_PMD_FINDING = "pmd_finding.gif";
	public static final String IMG_PRIORITY = "priority.gif";
	public static final String IMG_QUERY = "query.gif";
	public static final String IMG_REFRESH = "refresh.gif";
	public static final String IMG_RIGHT_ARROW_SMALL = "right_arrow_small.gif";
	public static final String IMG_SAVE_EDIT = "save_edit.gif";
	public static final String IMG_SAVEALL_EDIT = "saveall_edit.gif";
	public static final String IMG_SAVEAS_EDIT = "saveas_edit.gif";
	public static final String IMG_SIERRA_BLUE_PAGE = "sierra_blue_page.gif";
	public static final String IMG_SIERRA_DISCONNECT = "sierra_disconnect.gif";
	public static final String IMG_SIERRA_INVESTIGATE_DOT = "sierra_investigate_dot.gif";
	public static final String IMG_SIERRA_INVESTIGATE = "sierra_investigate.gif";
	public static final String IMG_SIERRA_LOGO = "sierra_logo.gif";
	public static final String IMG_SIERRA_ORANGE_PAGE = "sierra_orange_page.gif";
	public static final String IMG_SIERRA_POWERED_BY_SURELOGIC = "sierra_powered_by_surelogic.png";
	public static final String IMG_SIERRA_POWERED_BY_SURELOGIC_REALLY_SHORT = "sierra_powered_by_surelogic_really_short.png";
	public static final String IMG_SIERRA_POWERED_BY_SURELOGIC_SHORT = "sierra_powered_by_surelogic_short.png";
	public static final String IMG_SIERRA_PUBLISH = "sierra_publish.gif";
	public static final String IMG_SIERRA_SCAN = "sierra_scan.gif";
	public static final String IMG_SIERRA_SCAN_CLASS = "sierra_scan_class.gif";
	public static final String IMG_SIERRA_SCAN_DELTA = "sierra_scan_delta.gif";
	public static final String IMG_SIERRA_SCAN_PKG = "sierra_scan_pkg.gif";
	public static final String IMG_SIERRA_SERVER = "sierra_server.gif";
	public static final String IMG_SIERRA_SERVER_LOCAL = "sierra_server_local.gif";
	public static final String IMG_SIERRA_STAMP = "sierra_stamp.png";
	public static final String IMG_SIERRA_STAMP_SMALL = "sierra_stamp_small.gif";
	public static final String IMG_SIERRA_SYNC = "sierra_sync.gif";
	public static final String IMG_SORT_UP = "sort_up.gif";
	public static final String IMG_SORT_DOWN = "sort_down.gif";
	public static final String IMG_TEAM_SERVER_DECR = "team_server_decr.gif";
	public static final String IMG_TRAFFIC_LIGHT_GREEN = "traffic_light_green.png";
	public static final String IMG_TRAFFIC_LIGHT_RED = "traffic_light_red.png";
	public static final String IMG_TRAFFIC_LIGHT_YELLOW = "traffic_light_yellow.png";
	public static final String IMG_UP = "up.gif";

	public static final String IMG_ASSUME_DECR = "assume_decr.gif";
	public static final String IMG_CHOICE_ITEM = "choice_item.gif";
	public static final String IMG_CHOICE = "choice.gif";
	public static final String IMG_CLASS = "class.gif";
	public static final String IMG_CONSISTENT_DECR = "consistent_decr.gif";
	public static final String IMG_FOLDER = "folder.gif";
	public static final String IMG_INCONSISTENT_DECR = "inconsistent_decr.gif";
	public static final String IMG_INFO_DECR = "info_decr.gif";
	public static final String IMG_INFO = "info.gif";
	public static final String IMG_INTERFACE = "interface.gif";
	public static final String IMG_COMP_UNIT = "jcu.gif";
	public static final String IMG_LINK = "link.gif";
	public static final String IMG_PACKAGE = "package.gif";
	public static final String IMG_PLUS = "plus.gif";
	public static final String IMG_PROMISE = "promise.gif";
	public static final String IMG_REDDOT_DECR = "reddot_decr.gif";
	public static final String IMG_REDX = "redx.gif";
	public static final String IMG_TALLYHO = "tallyho.gif";
	public static final String IMG_TRUSTED_DECR = "trusted_decr.gif";
	public static final String IMG_UNKNOWN = "unknown.gif";
	public static final String IMG_VIRTUAL_DECR = "virtual_decr.gif";
	public static final String IMG_WARNING_DECR = "warning_decr.gif";
	public static final String IMG_WARNING = "warning.gif";

	public static URL getImageURL(String symbolicName) {
		final String pluginPath = PATH + symbolicName;
		URL url = CommonImages.class.getResource(pluginPath);
		if (url == null) {
			SLLogger.getLogger()
					.severe(
							"unable to create a URL for the plug-in path "
									+ pluginPath);
		}
		return url;
	}

	public static Icon getIcon(String symbolicName) {
		URL imgURL = getImageURL(symbolicName);
		if (imgURL != null) {
			return new ImageIcon(imgURL, symbolicName);
		}
		// nothing found
		return null;
	}

	public static Image getJavaImage(String symbolicName) {
		URL imgURL = getImageURL(symbolicName);
		if (imgURL != null) {
			return Toolkit.getDefaultToolkit().createImage(imgURL);
		}
		// nothing found
		return null;
	}
}
