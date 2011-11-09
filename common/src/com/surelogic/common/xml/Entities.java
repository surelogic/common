package com.surelogic.common.xml;

import java.util.*;

import com.surelogic.common.CharBuffer;

/**
 * Manages a table of entities for XML. This class can be used to help escape
 * strings that are output in XML format.
 */
public final class Entities {

	private static final Entities E;
	private static final Entities ASCII_CONTROL = new Entities(false);
		
	static {
		E = new Entities(true);
		E.defineStandardXML();
		
		// U+0009, U+000A, U+000D: these are the only C0 controls accepted in XML 1.0
		//RestrictedChar	   ::=   	[#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]		
		for(int i=0; i<9;i++) {			
			ASCII_CONTROL.define("\\"+i, String.valueOf((char) i));
		}
		ASCII_CONTROL.define("\\b", "\u000b");
		ASCII_CONTROL.define("\\c", "\u000c");
		for(int i=14; i<32;i++) {			
			ASCII_CONTROL.define("\\"+Integer.toHexString(i), String.valueOf((char) i));
		}
		for(int i=0x7F; i<=0x84;i++) {			
			ASCII_CONTROL.define("\\"+Integer.toHexString(i), String.valueOf((char) i));
		}
		for(int i=0x86; i<=0x9F;i++) {			
			ASCII_CONTROL.define("\\"+Integer.toHexString(i), String.valueOf((char) i));
		}
		for(int i=0xD800; i<0xE000;i++) {			
			ASCII_CONTROL.define("\\"+Integer.toHexString(i), String.valueOf((char) i));
		}
		ASCII_CONTROL.define("\\fffe", String.valueOf((char) 0xfffe));
		ASCII_CONTROL.define("\\ffff", String.valueOf((char) 0xffff));
		ASCII_CONTROL.generateCharMap();
	}

	private final boolean wrapForXML;
	
	private Entities(boolean wrap) {
		wrapForXML = wrap;
	}
	
	private void generateCharMap() {
		for(Tuple t : f_NameValue) {
			if (t instanceof CharValueTuple) {
				CharValueTuple cvt = (CharValueTuple) t; 				
				//System.out.println("Mapping "+((int) cvt.f_value)+" to "+cvt.f_name);
				f_map.put(Integer.valueOf((int) cvt.f_value), cvt.f_name);				
			}
		}
		checkCharMap();
	}
	
	private void checkCharMap() {
		for(Tuple t : f_NameValue) {
			if (t instanceof CharValueTuple) {
				CharValueTuple cvt = (CharValueTuple) t; 		
				String mapped = f_map.get(Integer.valueOf(cvt.f_value));
				if (mapped == null) {
					System.out.println("Couldn't find mapping for "+cvt.f_name);
				}
			}
		}
	}

	public static void start(final String name, final StringBuilder b) {
		start(name, b, 0);
	}
	
	public static void start(final String name, final StringBuilder b, int indent) {
		indent(b, indent);
		b.append('<').append(name);
	}

	public static void indent(final StringBuilder b, int indent) {
		for(int i=0; i<indent; i++) {
			b.append(XMLConstants.INDENT);
		}
	}
	
	public static void newLine(final StringBuilder b, int indent) {
		b.append('\n');
		indent(b, indent);
	}
	
	public static void closeStart(StringBuilder b, boolean end) {	
		closeStart(b, end, true);
	}
	
	public static void closeStart(StringBuilder b, boolean end, boolean newline) {		
		b.append(end ? "/>" : ">");
		if (newline) {
			b.append('\n');
		}
	}
	
	/**
	 * Helper to create <name\>enclosedText</name>
	 */
	public static void createTag(String name, String enclosedText,
			StringBuilder b) {
		b.append('<').append(name).append('>');
		addEscaped(enclosedText, b);
		end(name, b, 0);
	}

	public static void end(String name, StringBuilder b, int indent) {
		if (indent > 0) {
			indent(b, indent);
		}
		b.append("</").append(name).append(">\n");
	}
	
	/**
	 * Helper to avoid having to escape non-string values.
	 */
	private static void add(final String name, final String value,
			final StringBuilder b) {
		b.append(' ').append(name).append("=\"");
		b.append(value);
		b.append('\"');
	}

	public static void addAttribute(final String name, final String value,
			final StringBuilder b) {
		if (value == null) {
			return;
		}
		add(name, E.escape(value), b);
	}

	public static void addAttribute(final String name, final boolean value,
			final StringBuilder b) {
		add(name, Boolean.toString(value), b);
	}

	public static void addAttribute(final String name, final int value,
			final StringBuilder b) {
		add(name, Integer.toString(value), b);
	}

	public static void addAttribute(final String name, final long value,
			final StringBuilder b) {
		add(name, Long.toString(value), b);
	}

	public static void addEscaped(final String value, final StringBuilder b) {
		b.append(E.escape(value));
	}

	public static CharBuffer addEscaped(final String value, final CharBuffer b) {
		b.append(E.escape(value));
		return b;
	}

	public static String trimInternal(final String value) {
		return value.replaceAll("\\s+", " ");
	}

	/**
	 * A private type to store names and values that we want escaped.
	 */
	private abstract static class Tuple {
		final String f_name;
		final boolean wrapForXML;
		
		Tuple(boolean wrapForXML, final String name) {
			this.wrapForXML = wrapForXML;
			f_name = name;
		}

		/**
		 * Does the value appear in the given string, beginning at the given
		 * index?
		 */
		public abstract boolean testFor(String input, int idx);

		/**
		 * Get the length of the value.
		 */
		public abstract int getValueLength();

		/**
		 * Get the value.
		 */
		public final void appendName(StringBuilder sb) {
			if (wrapForXML) {
				sb.append('&');
			} 
			sb.append(f_name);
			if (wrapForXML) {
				sb.append(';');
			}
		}
	}

	private static final class CharValueTuple extends Tuple {
		final char f_value;

		CharValueTuple(boolean wrapForXML, final String name, final String value) {
			super(wrapForXML, name);
			if (value.length() != 1) {
				throw new IllegalArgumentException(
						"Value must have length of 1");
			}
			f_value = value.charAt(0);
		}

		@Override
		public boolean testFor(final String input, final int idx) {
			return input.charAt(idx) == f_value;
		}

		@Override
		public int getValueLength() {
			return 1;
		}
	}

	private static final class StringValueTuple extends Tuple {
		final String f_value;

		StringValueTuple(boolean wrapForXML, final String name, final String value) {
			super(wrapForXML, name);
			f_value = value;
		}

		@Override
		public boolean testFor(final String input, final int idx) {
			return input.substring(idx).startsWith(f_value);
		}

		@Override
		public int getValueLength() {
			return f_value.length();
		}
	}

	private final List<Tuple> f_NameValue = new ArrayList<Tuple>();
	private final Map<String,String> f_ValueName = new HashMap<String, String>();
	private Map<Integer,String> f_map = new HashMap<Integer,String>();


	public static String unescape(String c) {
		for(Map.Entry<String, String> e : E.f_ValueName.entrySet()) {
			c = c.replace(e.getKey(), e.getValue());
		}
		return c;
	}
	
	public String escape(final String text) {
		// Allocate space for original text plus 5 single-character entities
		final StringBuilder sb = new StringBuilder(text.length() + 10);
		int copyFromIdx = 0;
		int testForIdx = 0;
		while (testForIdx < text.length()) {
			boolean found = false;
			for (final Tuple t : f_NameValue) {
				if (t.testFor(text, testForIdx)) {
					// Copy test segment that is free of escapes
					if (copyFromIdx < testForIdx) {
						sb.append(text.substring(copyFromIdx, testForIdx));
					}
					// process escape
					t.appendName(sb);
					testForIdx += t.getValueLength();
					copyFromIdx = testForIdx;
					// Found the escape at this position, so stop looping over
					// escapes
					found = true;
					break;
				}
			}
			if (!found) {
				// No escapes match at the current position
				testForIdx += 1;
			}
		}
		// copy remaining text
		sb.append(text.substring(copyFromIdx));
		return sb.toString();
	}

	/**
	 * Defines a new character entity. For example, the default quotation is
	 * defined as:
	 * 
	 * <pre>
	 * Entities e = ...
	 * e.define(&quot;quot&quot;, &quot;\&quot;&quot;);
	 * </pre>
	 * 
	 * @param name
	 *            the name for the character entity.
	 * @param value
	 *            the value for the character entity.
	 */
	public void define(final String name, final String value) {
		assert name != null;
		assert value != null;
		final Tuple tuple = value.length() == 1 ? new CharValueTuple(wrapForXML, name,
				value) : new StringValueTuple(wrapForXML, name, value);
		f_NameValue.add(tuple);
		f_ValueName.put('&'+name+';', value);
	}

	/**
	 * Defines the five standard XML predefined character entities: &, ', >, <,
	 * ".
	 */
	public void defineStandardXML() {
		define("amp", "&");
		define("apos", "'");
		define("gt", ">");
		define("lt", "<");
		define("quot", "\"");
	}
	
	public static String escapeControlChars(String text) {
		return ASCII_CONTROL.convertChars(text);
		/*
		try {
			String rv = ASCII_CONTROL.escape(text);		
			return rv;
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
		*/
	}
	
	String convertChars(String text) {
		//checkCharMap();
		
		final int len = text.length();
		StringBuilder sb = new StringBuilder(len);
		for(int i=0; i<len; i++) {
			final char ch = text.charAt(i);
			String mapped = f_map.get(Integer.valueOf(ch));
			if (mapped != null) {
				sb.append(mapped);
			} else {
				/*
				if (ch == 0) {
					System.out.println("Leaving in zero");
					sb.append("\\0");
				} else {
				*/
					sb.append(ch);
				//}
			}
		}
		return sb.toString();
	}
}
