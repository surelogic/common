package com.surelogic.common.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.ILifecycle;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * A simple class that memorizes values and helps persist them across sessions.
 * This class is not intended as a crutch to avoid creating a principled data
 * model, but rather a class to help with the mechanics of persisting data. This
 * is a good class to use to persist view state that is not part of the view's
 * data model. For example, where a sash pane was located or which tab was
 * visible.
 */
public final class XMLMemo implements ILifecycle {

	static final String VERSION_1_0 = "1.0";

	static final String KEY = "key";
	static final String MEMO = "memo";
	static final String MEMO_BOOLEAN = "memo-boolean";
	static final String MEMO_INT = "memo-int";
	static final String MEMO_STRING = "memo-string";
	static final String VALUE = "value";
	static final String VERSION = "version";

	private final File f_file;

	public XMLMemo(final File file) {
		if (file == null)
			throw new IllegalArgumentException(I18N.err(44, "file"));
		f_file = file;
	}

	public void init() {
		try {
			load();
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(124, f_file.getAbsolutePath()), e);
		}
	}

	public void dispose() {
		try {
			persist();
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					I18N.err(125, f_file.getAbsolutePath()), e);
		}

	}

	private final Map<String, Boolean> f_keyToBoolean = new HashMap<String, Boolean>();

	public boolean getBoolean(final String key, final boolean defaultValue) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		Boolean resultObject = f_keyToBoolean.get(key);
		final boolean result;
		if (resultObject == null) {
			result = defaultValue;
		} else {
			result = resultObject;
		}
		return result;
	}

	public void setBoolean(final String key, final boolean value) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		f_keyToBoolean.put(key, value);
	}

	private final Map<String, Integer> f_keyToInteger = new HashMap<String, Integer>();

	public int getInt(final String key, final int defaultValue) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		Integer resultObject = f_keyToInteger.get(key);
		final int result;
		if (resultObject == null) {
			result = defaultValue;
		} else {
			result = resultObject;
		}
		return result;
	}

	public void setInt(final String key, final int value) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		f_keyToInteger.put(key, value);
	}

	private final Map<String, String> f_keyToString = new HashMap<String, String>();

	public String getString(final String key, final String defaultValue) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		String result = f_keyToString.get(key);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	public void setString(final String key, final String value) {
		if (key == null)
			throw new IllegalArgumentException(I18N.err(44, "key"));
		if (value == null)
			throw new IllegalArgumentException(I18N.err(44, "value"));
		f_keyToString.put(key, value);
	}

	private void persist() throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(f_file));
		pw.println("<?xml version='1.0' encoding='" + XMLConstants.ENCODING
				+ "' standalone='yes'?>");
		StringBuilder b = new StringBuilder();
		b.append("<").append(MEMO);
		Entities.addAttribute(VERSION, VERSION_1_0, b);
		b.append(">"); // don't end this element
		pw.println(b.toString());
		for (Map.Entry<String, Boolean> entry : f_keyToBoolean.entrySet()) {
			b = new StringBuilder();
			b.append("  <").append(MEMO_BOOLEAN);
			Entities.addAttribute(KEY, entry.getKey(), b);
			Entities.addAttribute(VALUE, entry.getValue().toString(), b);
			b.append("/>");
			pw.println(b.toString());
		}
		for (Map.Entry<String, Integer> entry : f_keyToInteger.entrySet()) {
			b = new StringBuilder();
			b.append("  <").append(MEMO_INT);
			Entities.addAttribute(KEY, entry.getKey(), b);
			Entities.addAttribute(VALUE, entry.getValue(), b);
			b.append("/>");
			pw.println(b.toString());
		}
		for (Map.Entry<String, String> entry : f_keyToString.entrySet()) {
			b = new StringBuilder();
			b.append("  <").append(MEMO_STRING);
			Entities.addAttribute(KEY, entry.getKey(), b);
			Entities.addAttribute(VALUE, entry.getValue(), b);
			b.append("/>");
			pw.println(b.toString());
		}
		pw.println("</" + MEMO + ">");
		pw.close();
	}

	private void load() throws Exception {
		final InputStream stream;
		try {
			stream = new FileInputStream(f_file);
		} catch (FileNotFoundException e) {
			/*
			 * If the file doesn't exist we will create it when we exit. This is
			 * not a problem, it just means it is the first time we have used
			 * this memento instance.
			 */
			return;
		}
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final DefaultHandler handler = new MemoReader();
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(stream, handler);
		} finally {
			stream.close();
		}
	}

	private class MemoReader extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (name.equals(MEMO)) {
				// nothing to do until there is more than one file version
			} else if (name.equals(MEMO_BOOLEAN)) {
				final String key = attributes.getValue(KEY);
				final String value = attributes.getValue(VALUE);
				if (key == null)
					throw new SAXException(I18N.err(44, KEY));
				if (value == null)
					throw new SAXException(I18N.err(44, VALUE));
				final boolean booleanValue = Boolean.parseBoolean(value);
				f_keyToBoolean.put(key, booleanValue);
			} else if (name.equals(MEMO_INT)) {
				final String key = attributes.getValue(KEY);
				final String value = attributes.getValue(VALUE);
				if (key == null)
					throw new SAXException(I18N.err(44, KEY));
				if (value == null)
					throw new SAXException(I18N.err(44, VALUE));
				final int intValue = Integer.parseInt(value);
				f_keyToInteger.put(key, intValue);
			} else if (name.equals(MEMO_STRING)) {
				final String key = attributes.getValue(KEY);
				final String value = attributes.getValue(VALUE);
				if (key == null)
					throw new SAXException(I18N.err(44, KEY));
				if (value == null)
					throw new SAXException(I18N.err(44, VALUE));
				f_keyToString.put(key, value);
			}
		}
	}
}
