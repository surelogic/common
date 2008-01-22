package com.surelogic.common.xml;

import java.io.IOException;
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
  
  public static void parseResource(Logger log, SAXParser sp, String resource,
                                   DefaultHandler handler, String errMsg) {
    try {
      sp.parse(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(resource), handler);
    } catch (SAXException e) {
      log.log(Level.WARNING, errMsg, e);
    } catch (IOException e) {
      log.log(Level.WARNING, errMsg, e);
    }
  }
}
