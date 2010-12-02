package com.surelogic.common.xml;

import java.util.*;

import org.xml.sax.Attributes;

public class Entity {
	final String name;
	protected final Map<String,String> attributes = new HashMap<String,String>();		
	final String id;
	final List<MoreInfo> infos = new ArrayList<MoreInfo>(0);
	final List<Entity> refs = new ArrayList<Entity>(0);
	SourceRef source;
	
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
		id = attributes.get(XMLConstants.ID_ATTR);
	}
	
	public Entity(String name, Map<String,String> a) {
		this.name = name;
		if (attributes != null) {
			attributes.putAll(a);
		}
		id = attributes.get(XMLConstants.ID_ATTR);
	}
	
	@Override 
	public final String toString() {
		return name;
	}
	
	public final String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}

	public void setSource(SourceRef r) {
		source = r;
	}

	public void addInfo(MoreInfo e) {
		infos.add(e);
	}
	
	public void addRef(Entity e) {
		refs.add(e);
	}
	
	public final int numRefs() {
		return refs.size();
	}
	
	public final Iterable<Entity> getReferences() {
		return refs;
	}

	public final Map<String, String> getAttributes() {
		return attributes;
	}
	
	public final String getAttribute(String a) {
		return attributes.get(a);
	}
		
	public SourceRef getSource() {
		return source;
	}
	
	public Collection<MoreInfo> getInfos() {
		return infos;
	}
	
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

