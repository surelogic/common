package com.surelogic.common.xml;

import java.util.Map;

public class SourceRef {
	final String line;
	final Map<String,String> attributes;
	
	public SourceRef(Entity e) {
		line = e.getAttribute(XMLConstants.LINE_ATTR);
		attributes = e.getAttributes();
	}
	
	public String getLine() {
		return line;
	}
	
	public String getAttribute(String a) {
		return attributes.get(a);
	}
}
