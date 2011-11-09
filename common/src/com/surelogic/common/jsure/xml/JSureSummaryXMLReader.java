package com.surelogic.common.jsure.xml;

import org.xml.sax.Attributes;

import com.surelogic.common.xml.IXMLResultListener;

public class JSureSummaryXMLReader extends AbstractXMLReader {
	public static final String ROOT = "sea-summary";

	public static final String TIME_ATTR = "time";
	public static final String OFFSET_ATTR = "offset";
	
	public JSureSummaryXMLReader(IXMLResultListener l) {
		super(l);
	}

	@Override
	protected String checkForRoot(String name, Attributes attributes) {
		if (ROOT.equals(name)) {
			if (attributes == null) {
				return "";
			}
			return attributes.getValue(TIME_ATTR);
		}
		return null;
	}
}
