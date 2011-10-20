package com.surelogic.common.xml;

import java.util.Map;

import com.surelogic.common.jsure.xml.AbstractXMLReader;

public class SourceRef {
	final String line;
	final Map<String,String> attributes;
	
	public SourceRef(Entity e) {
		line = e.getAttribute(XMLConstants.LINE_ATTR);
		attributes = e.getAttributes();		
		maybeReplace(AbstractXMLReader.FILE_ATTR);
		maybeReplace(AbstractXMLReader.URI_ATTR);
		maybeReplace(AbstractXMLReader.PATH_ATTR);
		maybeReplace(AbstractXMLReader.PKG_ATTR);
		maybeReplace(AbstractXMLReader.CUNIT_ATTR);
		maybeReplace(AbstractXMLReader.JAVA_ID_ATTR);
		maybeReplace(AbstractXMLReader.WITHIN_DECL_ATTR);
	}
	
	private void maybeReplace(String attr) {
		String value  = attributes.get(attr);
		if (value == null) {
			return;
		}
		String intern = Entity.maybeIntern(value);
		if (intern != value) {
			attributes.put(attr, intern);
		}
	}

	public String getLine() {
		return line;
	}
	
	public String getAttribute(String a) {
		return attributes.get(a);
	}
}
