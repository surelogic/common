package com.surelogic.common.xml;

public class MoreInfo {
	public final SourceRef source;
	public final String message;
	
	public MoreInfo(Entity e) {
		source = e.getSource();
		message = e.getAttribute(XMLConstants.MESSAGE_ATTR);
	}
}
