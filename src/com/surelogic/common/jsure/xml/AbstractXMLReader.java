package com.surelogic.common.jsure.xml;

import com.surelogic.common.xml.*;

public abstract class AbstractXMLReader extends NestedXMLReader {
	public static final String DROP = "drop";
	public static final String CU_DROP = "cu-drop";
	public static final String IR_DROP = "ir-drop";
	public static final String PROOF_DROP = "proof-drop";
	public static final String PROMISE_DROP = "promise-drop";
	public static final String RESULT_DROP = "result-drop";
	
	public static final String MESSAGE_ATTR = "message";
	public static final String MESSAGE_ID_ATTR = "message-id";
	public static final String TYPE_ATTR = "type";
	public static final String FULL_TYPE_ATTR = "full-type";
	public static final String CATEGORY_ATTR = "category";	
	
	public static final String FILE_ATTR = "file";
	public static final String LINE_ATTR = "line";
	public static final String HASH_ATTR = "hash";
	public static final String CONTEXT_ATTR = "context";
	
	public static final String PATH_ATTR = "path";
	public static final String URI_ATTR  = "uri";
	public static final String OFFSET_ATTR = "offset";
	public static final String PKG_ATTR = "pkg";
	public static final String CUNIT_ATTR = "cu";
	
	public static final String PROVED_ATTR = "proved-consistent";
	public static final String USES_RED_DOT_ATTR = "uses-red-dot";
	public static final String RESULT_ATTR = "result-type";
	
	public static final String FLAVOR_ATTR = "flavor";
	
	public static final String PROJECTS = "projects";
	
	/**
	 * @param l The listener that handles the top-level elements
	 */
	protected AbstractXMLReader(IXMLResultListener l) {
		super(l);	
	}
	
	protected AbstractXMLReader() {
		super();
	}
}
