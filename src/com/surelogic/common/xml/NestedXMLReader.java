package com.surelogic.common.xml;

/**
 * A simple reader that simply stores nested elements as "refs"
 * 
 * @author Edwin
 */
public abstract class NestedXMLReader extends XMLReader {
	/**
	 * @param l The listener that handles the top-level elements
	 */
	protected NestedXMLReader(IXMLResultListener l) {
		super(l);	
	}
	
	protected NestedXMLReader() {
		super();
	}
	
	protected void handleNestedEntity(Entity next, Entity last, String lastName) {
		next.addRef(last);
	}
}
