package com.surelogic.common.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Also please see the {@link Entities} as there is a lot of overlap (that class
 * does escaping properly for XML).
 */
public class XMLUtil {

  public static SAXParser createSAXParser() {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(true);
    spf.setNamespaceAware(true);
    SAXParser sp;
    try {
      sp = spf.newSAXParser();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Could not create SAX parser.", e);
    } catch (SAXException e) {
      throw new IllegalStateException("Could not create SAX parser.", e);
    }
    return sp;
  }

  public static boolean parseResource(Logger log, SAXParser sp, InputStream in, DefaultHandler handler, String errMsg) {
    try {
      sp.parse(in, handler);
      return true;
    } catch (SAXException e) {
      log.log(Level.WARNING, errMsg, e);
      return false;
    } catch (IOException e) {
      log.log(Level.WARNING, errMsg, e);
      return false;
    }
  }

  public static String openNode(String tag) {
    return "<" + tag + ">";
  }

  public static String startNode(String tag) {
    return "<" + tag + " ";
  }

  public static String closeNode(String tag) {
    return "</" + tag + ">";
  }

  /**
   * Includes a new line
   */
  public static String oneAttrNode(String tag, String attr, String val) {
    return startNode(tag) + lastAttr(attr, val);
  }

  public static String oneAttrOpen(String tag, String attr, String val) {
    return startNode(tag) + setAttr(attr, val) + ">\n";
  }

  public static String setAttr(String attr, String val) {
    return attr + "=\"" + val + "\"";
  }

  /**
   * Includes a new line
   */
  public static String lastAttr(String attr, String val) {
    return attr + "=\"" + val + "\"/>\n";
  }

  private XMLUtil() {
    // no instances
  }
}
