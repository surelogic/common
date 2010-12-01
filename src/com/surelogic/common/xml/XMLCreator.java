package com.surelogic.common.xml;

import java.io.*;
import java.util.*;

public class XMLCreator {
	protected final Map<String,String> attributes = new HashMap<String,String>();
	protected final StringBuilder b = new StringBuilder();
	protected boolean firstAttr = true;
	protected final PrintWriter pw;
	
	protected XMLCreator(OutputStream out) throws IOException {
		if (out != null) {
			pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
			pw.println("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		} else {
			pw = null;
		}
	}
		
	protected final void flushBuffer(PrintWriter pw) {
		if (pw != null) {
			pw.println(b.toString());
			reset();
		}
	}
	
	protected final void reset() {
		b.setLength(0);
		firstAttr = true;
		attributes.clear();
	}
	 
	public final void addAttribute(String name, boolean value) {
		if (value) {
			addAttribute(name, Boolean.toString(value));
		}
	}
	
	public final boolean addAttribute(String name, Long value) {		
		if (value == null) {
			return false;
		}
		addAttribute(name, value.toString());		
		return true;
	}
	
	public final void addAttribute(String name, String value) {
		if (firstAttr) {
			firstAttr = false;
		} else {
			b.append("\n\t");
		}
		Entities.addAttribute(name, value, b);
		attributes.put(name, value);
	}
}
