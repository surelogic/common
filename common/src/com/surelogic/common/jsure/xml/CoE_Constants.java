package com.surelogic.common.jsure.xml;

import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.XMLUtil;

public final class CoE_Constants {
  public static final Logger LOG = SLLogger.getLogger("xml.results.coe");

  public static final String ENCODING = "UTF-8";
  
  /**
   * Second part of the name refers to the type of attribute
   */
  public static final String ATTR_ID   = "id";
  public static final String ATTR_NAME = "name";
  
  public static final String ROOT_TAG  = "root";
  
  /*
   * Tags for results
   */
  public static final String RESULT_TAG       = "result";
 
  public static final String ATTR_MESSAGE     = "msg";
  public static final String ATTR_SOURCE      = "source";
  public static final String ATTR_CVSSOURCE   = "cvssource";
  public static final String ATTR_CVSREVISION = "rev";
  public static final String ATTR_LINE_NUM    = "line";
  public static final String ATTR_BASE_IMAGE  = "baseImage";
  public static final String ATTR_FLAGS       = "flags";
  public static final String ATTR_REDDOTS     = "reddots";
  public static final String ATTR_STATUS      = "status";
 
  public static final String RESULT_BACK_LINK = 
    XMLUtil.startNode(RESULT_TAG)+ATTR_MESSAGE+"=\"(Skipped reference to parent node to avoid loop)\"/>";

  public static final String MESSAGE_TAG_FMT  = ATTR_MESSAGE+"=\"%s\"";
  public static final String SOURCE_TAG_FMT   = " "+ATTR_SOURCE+"=\"%s.html\"";
  public static final String CVSSRC_TAG_FMT   = " "+ATTR_CVSSOURCE+"=\"%s\"";
  public static final String CVSREV_TAG_FMT   = " "+ATTR_CVSREVISION+"=\"%s\"";
  public static final String LINENO_TAG_FMT   = " "+ATTR_LINE_NUM+"=\"%s\"";
  public static final String IMAGE_TAG_FMT    = " "+ATTR_BASE_IMAGE+"=\"%s\"";
  public static final String FLAGS_TAG_FMT    = " "+ATTR_FLAGS+"=\"%s\"";
  public static final String REDDOT_TAG_FMT   = " "+ATTR_REDDOTS+"=\"%s\"";
  
  /** Flag to render no adornment */
  public final static int NONE = 0;
  
  /** Flag to render the assume (A) adornment */
  public final static int ASSUME = 0x001;

  /** Flag to render the consistent (+) adornment */
  public final static int CONSISTENT = 0x002;

  /** Flag to render the inconsistent (X) adornment */
  public final static int INCONSISTENT = 0x004;

  /** Flag to render the reddot adornment */
  public final static int REDDOT = 0x008;

  /** Flag to render the trusted adornment */
  public final static int TRUSTED = 0x010;

  /** Flag to render the virtual adornment */
  public final static int VIRTUAL = 0x020;

  /** Flag to render the warning adornment */
  public final static int INFO = 0x040;

  /** Flag to render the warning adornment */
  public final static int INFO_WARNING = 0x080;
  
  /*
   * Tags for source files
   * Duplicated in common-eclipse/SourceZip
   */
  public static final String SRCFILES_TAG   = "sourceFiles";
  public static final String PACKAGE_TAG    = "package";
  public static final String CLASS_TAG      = "class";
  public static final String PACKAGE_FORMAT = "\t<"+PACKAGE_TAG+" name=\"%s\">\n";
  public static final String CLASS_FORMAT   = "\t\t<"+CLASS_TAG+" name=\"%s\" source=\"%s\"/>\n";  
  
  /*
   * Tags for red dots
   */
  public static final String REDDOTS_TAG   = "reddots";
  public static final String REDDOT_TAG    = "reddot";
  public static final String REDDOT_FORMAT = "\t<"+REDDOT_TAG+" id=\"%s\" name=\"%s\" image=\"%s\"/>\n";
  public static final String ATTR_IMAGE    = "image";
  
  /*
   * Tags for analyses
   */
  public static final String ANALYSES_TAG = "analyses";
  public static final String ANALYSIS_TAG = "analysis";
  public static final String ANALYSIS_FORMAT = "\t<"+ANALYSIS_TAG+" id=\"%s\" />\n";
}
