package com.surelogic.common.xml;

import java.util.*;

import org.xml.sax.Attributes;

public class Entity {
	protected static final Map<String,String> interned = Collections.synchronizedMap(new HashMap<String, String>());
	public static String internString(String v) {
		interned.put(v, v);
		return v;
	}
	static {
		internString("true");
		internString("false");
	}
	
	protected void maybeReplace(String attr) {
		String value  = attributes.get(attr);
		String intern = Entity.maybeIntern(value);
		if (intern != value) {
			attributes.put(attr, intern);
		}
	}
	
	final String name;
	protected final Map<String,String> attributes = new HashMap<String,String>(4, 1.0f);
	final String id;
	List<MoreInfo> infos = Collections.emptyList();
	List<Entity> refs = Collections.emptyList();
	SourceRef source;
	StringBuilder chars;
	String contents;	
	
	public final void addToCData(char[] buf, int offset, int len) {
		if (chars == null) {
			chars = new StringBuilder(len);
		}
		chars.append(buf, offset, len);
	}
	
	public String getCData() {
		if (contents == null) {
			if (chars == null) {
				contents = null;
			} else {
				contents = chars.toString();
			}
		}
		return contents;
	}
	
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
				attributes.put(aName.intern(), getInternValue(aValue));
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
	
	protected String getInternValue(String v) {		
		String intern = interned.get(v);
		if (intern != null) {
			return intern;
		}
		return v;
	}
	
	public static String maybeIntern(String v) {
		String intern = interned.get(v);
		if (intern != null) {
			return intern;
		}
		internString(v);
		return v;
	}
	
	@Override 
	public final String toString() {
		return name;
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getEntityName() {
		return getName();
	}
	
	public String getId() {
		return id;
	}

	public void setSource(SourceRef r) {
		source = r;
	}

	public void addInfo(MoreInfo e) {
		if (infos.isEmpty()) {
			infos = new ArrayList<MoreInfo>(1);
		}
		infos.add(e);
	}
	
	public void addRef(Entity e) {
		if (refs.isEmpty()) {
			refs = new ArrayList<Entity>(1);
		}
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
