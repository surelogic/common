package com.surelogic.common.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.FileUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entity;
import com.surelogic.common.xml.XmlReader;

public abstract class XmlReader extends DefaultHandler {
  public static final String PROJECT_ATTR = "project";
  public static final String ID_ATTR = "id";

  /**
   * Constructs a new instance.
   * 
   * @param l
   *          the listener handling the top-level elements
   */
  protected XmlReader(IXmlResultListener l) {
    listener = l;
  }

  /**
   * Constructs a new instance.
   */
  protected XmlReader() {
    listener = (IXmlResultListener) this;
  }

  private final Stack<Entity> inside = new Stack<Entity>();

  private final IXmlResultListener listener;

  public final void read(File location) throws Exception {
    InputStream stream;
    try {
      stream = new FileInputStream(location);

      if (location.getName().endsWith(FileUtility.GZIP_SUFFIX)) {
        /*
         * There appear to be problems using the gzip stream without changing
         * the buffer size. The below ~65K buffer seems to fix the load issues
         * that JSure experienced intermittently with .gz files.
         */
        stream = new GZIPInputStream(stream, 65536);
      }
    } catch (FileNotFoundException e) {
      return;
    }
    read(stream);
  }

  public final void read(InputStream stream) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      // Parse the input
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(stream, this);
    } finally {
      listener.done();
      stream.close();
    }
  }

  @Override
  public final void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    String uid = checkForRoot(name, attributes);
    if (uid != null) {
      final String proj = attributes.getValue(PROJECT_ATTR);
      listener.start(uid, proj);
      return;
    }
    inside.push(listener.makeEntity(name, attributes));
  }

  @Override
  public void characters(char buf[], int offset, int len) throws SAXException {
    if (!inside.isEmpty()) {
      Entity e = inside.peek();
      e.addToCData(buf, offset, len);
    }
  }

  @Override
  public final void endElement(String uri, String localName, String name) throws SAXException {
    if (checkForRoot(name, null) != null) {
      return;
    }
    final Entity outer = inside.pop();
    if (!outer.getName().equals(name)) {
      SLLogger.getLogger().log(Level.WARNING, name + " doesn't match " + outer, new Exception());
    } else {
      if (!inside.isEmpty()) {
        final Entity inner = inside.peek();
        handleNestedEntity(inner, outer, name);
      } else if (listener != null) {
        listener.notify(outer);
      }
    }
  }

  /**
   * Detect the root element
   * 
   * @return non-null uid if it's the root element
   */
  protected abstract String checkForRoot(String name, Attributes attributes);

  /**
   * Called to do any extra processing to relate the enclosing and nested
   * entities
   * 
   * @param inner
   *          The nested entity
   * @param outer
   *          The enclosing entity
   * @param outerName
   *          the name of <tt>outer</tt> should be the same as
   *          <tt>outer.getName()</tt>.
   */
  protected void handleNestedEntity(Entity inner, Entity outer, String outerName) {
    inner.addRef(outer);
  }
}
