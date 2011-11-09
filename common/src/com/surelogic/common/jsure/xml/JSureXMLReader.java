package com.surelogic.common.jsure.xml;

import org.xml.sax.Attributes;

import com.surelogic.common.refactor.*;
import com.surelogic.common.xml.*;

public class JSureXMLReader extends AbstractXMLReader {
	public static final String ROOT = "sea-snapshot";

	public static final String SOURCE_REF = "source-ref";
	public static final String SUPPORTING_INFO = "supporting-info";	
	public static final String JAVA_DECL_INFO = "java-decl-info";
	public static final String DEPONENT = "deponent";
	public static final String DEPENDENT = "dependent";
	
	public static final String UID_ATTR = "uid";
	public static final String ID_ATTR = "id";
	
	public JSureXMLReader(IXMLResultListener l) {
		super(l);
	}


	@Override
	protected String checkForRoot(String name, Attributes attributes) {
		if (ROOT.equals(name)) {
			if (attributes == null) {
				return "";
			}
			return attributes.getValue(UID_ATTR);
		}
		return null;
	}

	@Override
	protected void handleNestedEntity(Entity next, Entity last, String lastName) {
		if (SOURCE_REF.equals(lastName)) {
			String flavor = last.getAttribute(FLAVOR_ATTR);
			if (flavor != null) {
				//throw new UnsupportedOperationException();
				next.addRef(last);
			} else {
				next.setSource(new SourceRef(last));
			}
		}
		else if (SUPPORTING_INFO.equals(lastName)) {
			next.addInfo(new MoreInfo(last));
		}
		else if (JAVA_DECL_INFO.equals(lastName)) {
			JavaDeclInfo info = (JavaDeclInfo) last;
			IJavaDeclInfoClient client = (IJavaDeclInfoClient) next;
			client.addInfo(info);
		}
		else {
			//System.out.println("Finished '"+name+"' inside of "+next);		
			next.addRef(last);
		}
	}
}
