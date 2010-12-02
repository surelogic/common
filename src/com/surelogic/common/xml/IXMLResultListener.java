package com.surelogic.common.xml;

import org.xml.sax.Attributes;

import com.surelogic.common.xml.Entity;

public interface IXMLResultListener {
	/**
	 * Called upon seeing the root element
	 */
	void start(String uid, String project);
	
	/**
	 * Called for each top-level entity	 
	 */
	void notify(Entity e);
	
	/**
	 * Called upon seeing the end of the root element
	 */
	void done();
	
	Entity makeEntity(String name, Attributes a);
}
