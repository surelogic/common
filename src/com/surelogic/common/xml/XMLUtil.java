package com.surelogic.common.xml;

import static com.surelogic.common.xml.XMLConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
  
  public static void parseResource(Logger log, SAXParser sp, 
                                   InputStream in,
                                   DefaultHandler handler, String errMsg) {
    try {
      sp.parse(in, handler);
    } catch (SAXException e) {
      log.log(Level.WARNING, errMsg, e);
    } catch (IOException e) {
      log.log(Level.WARNING, errMsg, e);
    }
  }
  
  public static String openNode(String tag) {
	  return "<"+tag+">";
  }

  public static String startNode(String tag) {
	  return "<"+tag+" ";
  }

  public static String closeNode(String tag) {
	  return "</"+tag+">";
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
	  return attr+"=\""+val+"\"";
  }

  /**
   * Includes a new line
   */
  public static String lastAttr(String attr, String val) {
	  return attr+"=\""+val+"\"/>\n";
  }

  public static String escape(String s) {
	  StringBuilder result = new StringBuilder(s.length() + 20);
	  char c;
	  for (int i = 0; i < s.length(); ++i) {
		  switch (c = s.charAt(i)) {
		  case '<':
			  result.append(LESS_THAN);
			  break;
		  case '>':
			  result.append(GREATER_THAN);
			  break;
		  case '"':
			  result.append(DOUBLE_QUOTE);
			  break;
		  case '\'':
			  result.append(APOSTROPHE);
			  break;
		  case '&':
			  result.append(AMPERSAND);
			  break;
		  default:
			  result.append(c);
		  }
	  }
	  return result.toString();
  }

  public static String unescape(String str) {
	  StringBuffer result = new StringBuffer(str.length());
	  for(int i=0; i<str.length(); i++) {
		  char c = str.charAt(i);
		  if(c != '&') result.append(c);
		  else {
			  int entityEnd = str.indexOf(';', i);
			  if(entityEnd == -1) {
				  // (bad) unquoted &, just add it and continue
				  result.append('&');
				  continue;
			  }
			  String entity = str.substring(i, entityEnd + 1);
			  if(entity.equals(LESS_THAN)) result.append('<');
			  else if (entity.equals(GREATER_THAN)) result.append('>');
			  else if (entity.equals(DOUBLE_QUOTE)) result.append('"');
			  else if (entity.equals(APOSTROPHE)) result.append('\'');
			  else if (entity.equals(AMPERSAND)) result.append('&');
			  i = entityEnd;
		  }
	  }
	  return result.toString();
  }  
}
