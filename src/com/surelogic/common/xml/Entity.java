package com.surelogic.common.xml;

import java.util.*;

import org.xml.sax.Attributes;

public class Entity<T extends Entity<T>> {
	final String name;
	protected final Map<String,String> attributes = new HashMap<String,String>();		
	//final String id;
	//final List<Info> infos = new ArrayList<Info>(0);
	final List<T> refs = new ArrayList<T>(0);
	//SourceRef source;
	
	public static String getValue(Attributes a, String name) {
		for (int i = 0; i < a.getLength(); i++) {
			final String aName = a.getQName(i);
			if (name.equals(aName)) {						
				final String aValue = a.getValue(i);
				return aValue;
			}
		}
		return null;
	}
	
	public Entity(String name, Attributes a) {
		this.name = name;
		if (attributes != null) {
			for (int i = 0; i < a.getLength(); i++) {
				final String aName = a.getQName(i);
				final String aValue = a.getValue(i);
				attributes.put(aName, aValue);
			}
		}
		//id = attributes.get(JSureXMLReader.ID_ATTR);
	}
	
	public Entity(String name, Map<String,String> a) {
		this.name = name;
		if (attributes != null) {
			attributes.putAll(a);
		}
		//id = attributes.get(JSureXMLReader.ID_ATTR);
	}
	
	@Override 
	public final String toString() {
		return name;
	}
	
	public final String getName() {
		return name;
	}
	
	/*
	public String getId() {
		return id;
	}
	
	void setSource(SourceRef r) {
		source = r;
	}
	
	void addInfo(Info e) {
		infos.add(e);
	}
	*/
	
	public final void addRef(T e) {
		refs.add(e);
	}
	
	public final Iterable<T> getReferences() {
		return refs;
	}

	public final Map<String, String> getAttributes() {
		return attributes;
	}
	
	public final String getAttribute(String a) {
		return attributes.get(a);
	}
	
	/*
	public SourceRef getSource() {
		return source;
	}
	
	public Collection<Info> getInfos() {
		return infos;
	}
	*/
	
	private final String DIFF_STATUS = "DiffStatus";
	private final String OLD = "Old";
	private final String NEWER = "New";
	
	public final void setAsOld() {
		attributes.put(DIFF_STATUS, OLD);
	}
	
	public final void setAsNewer() {
		attributes.put(DIFF_STATUS, NEWER);
	}
	
	public final boolean isOld() {
		return attributes.get(DIFF_STATUS) == OLD;
	}
	
	public final boolean isNewer() {
		return attributes.get(DIFF_STATUS) == NEWER;
	}
	
	public final String getDiffStatus() {
		String rv = attributes.get(DIFF_STATUS);
		if (rv == null) {
			return "";
		}
		return rv;
	}
}

