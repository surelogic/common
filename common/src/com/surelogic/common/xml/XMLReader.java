package com.surelogic.common.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLReader extends DefaultHandler {
	public static final String PROJECT_ATTR = "project";

	protected final Stack<Entity> inside = new Stack<Entity>();
	protected final IXMLResultListener listener;

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
			saxParser.parse(stream, this);
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
	public final void endElement(String uri, String localName, String name)
			throws SAXException {
		if (checkForRoot(name, null) != null) {
			return;
		}
		Entity last = inside.pop();
		if (!last.getName().equals(name)) {
			System.out.println(name + " doesn't match " + last);
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
	 * Called to do any extra processing to relate the enclosing and nested
	 * entities
	 * 
	 * @param next
	 *            The nested entity
	 * @param last
	 *            The enclosing entity
	 * @param lastName
	 */
	protected void handleNestedEntity(Entity next, Entity last, String lastName) {
		throw new UnsupportedOperationException(
				"handleNestedEntity() not overridden -- There should be no nested entities");
	}
}
