package com.surelogic.common.xml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.logging.SLLogger;

public abstract class XMLReader extends DefaultHandler implements LexicalHandler {
	public static final String PROJECT_ATTR = "project";
	public static final String COMMENT_TAG = "<comment>";
	
	protected final Stack<Entity> inside = new Stack<Entity>();
	protected final IXMLResultListener listener;
	
	/**
	 * @param l The listener that handles the top-level elements
	 */
	protected XMLReader(IXMLResultListener l) {
		listener = l;		
	}
	
	protected XMLReader() {
		listener = (IXMLResultListener) this;
	}
	
	public final void read(File location) throws Exception {
		InputStream stream;
		try {
			stream = new FileInputStream(location);
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
			if (this instanceof LexicalHandler) {
				org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this); 
			}
			saxParser.parse(stream, this);
		} catch (SAXException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Problem parsing JSure XML file", e);
		} catch (ParserConfigurationException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Problem parsing JSure XML file", e);
		} finally {
			listener.done();
			stream.close();
		}
	}
	
	@Override
	public final void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		String uid = checkForRoot(name, attributes);
		if (uid != null) {
			final String proj = attributes.getValue(PROJECT_ATTR);
			listener.start(uid, proj);
			return;
		}
		//System.out.println("Read '"+name+"'");
		
		if (!inside.isEmpty()) {
			/*
			Entity last = inside.peek();
			System.out.println("Started '"+name+"' inside of "+last);
			*/
		}
		inside.push(listener.makeEntity(name, attributes));//new Entity(name, attributes));
	}
	
	@Override
	public void characters(char buf[], int offset, int len) throws SAXException {
		if (!inside.isEmpty()) {
			Entity e = inside.peek();
			e.addToCData(buf, offset, len);
		}
	}
	
	@Override
	public final void endElement(String uri, String localName, String name)
	throws SAXException {
		if (checkForRoot(name, null) != null) {
			return;
		}
		Entity last = inside.pop();
		if (!last.getName().equals(name)) {
			System.out.println(name+" doesn't match "+last);
		} else {
			if (!inside.isEmpty()) {
				Entity next = inside.peek();
				handleNestedEntity(next, last, name);
			} else if (listener != null) {
				listener.notify(last);
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
	 * Called to do any extra processing to relate the enclosing and nested entities
	 * @param next The nested entity
	 * @param last The enclosing entity
	 * @param lastName
	 */
	protected void handleNestedEntity(Entity next, Entity last, String lastName) {
		throw new UnsupportedOperationException("There should be no nested entities");
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		if (!inside.isEmpty()) {
			Entity e = inside.peek();
			String c = new String(ch, start, length);
			Map<String,String> a = new HashMap<String,String>(1);
			a.put(COMMENT_TAG, c);
			e.addRef(new Entity(COMMENT_TAG, a));
		}
	}

	@Override
	public void endCDATA() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDTD() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startCDATA() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
	}
}
