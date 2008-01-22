package com.surelogic.common.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

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
}
