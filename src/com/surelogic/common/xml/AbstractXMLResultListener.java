package com.surelogic.common.xml;

import java.util.*;

import org.xml.sax.Attributes;

import com.surelogic.common.xml.Entity;

public abstract class AbstractXMLResultListener implements IXMLResultListener {	
	/**
	 * Holds onto the ref data until the reference is defined
	 */
	static class Ref {
		final String from;
		final int fromId;
		final Entity ref;
		
		public Ref(String from, int id, Entity ref) {
			this.from = from;
			this.fromId = id;
			this.ref  = ref;
		}
	}
	private static final List<Ref> DEFINED = Collections.<Ref>emptyList();
	private static final List<Ref> OMITTED = new ArrayList<Ref>(0); 
	
	private final Map<String,List<Ref>> references = new HashMap<String,List<Ref>>();
	
	private int processEntity(Entity e) {
		final int id = Integer.valueOf(e.getId());
		final List<Ref> danglingRefs = references.get(e.getId());

		final boolean defined = define(id, e);
		references.put(e.getId(), defined ? DEFINED : OMITTED);

		if (defined && danglingRefs != null) {
			for(Ref r : danglingRefs) {
				handleRef(r.from, r.fromId, r.ref);
			}
		}
		return id;
	}
	
	/**
	 * @return true if created entity
	 */
	protected abstract boolean define(int id, Entity e);
	
	/**
	 * Create a reference from X to Y
	 */
	protected abstract void handleRef(String from, int fromId, Entity to);
	
	private void processRef(String from, int fromId, Entity to) {
		List<Ref> refs = references.get(to.getId());
		if (refs == null) {
			refs = new ArrayList<Ref>();
			references.put(to.getId(), refs);
		}
		else if (refs == DEFINED) {
			handleRef(from, fromId, to);
			return;
		}
		else if (refs == OMITTED) {
			return;
		}
		refs.add(new Ref(from, fromId, to));
	}
	
	public void start(String uid, String project) {
		// Do nothing
	}
	
	public void notify(Entity e) {
		//System.out.println("Got "+e);
		final int id = processEntity(e);
		
		for(Entity ref : e.getReferences()) {
			processRef(e.getId(), id, ref);
		}
	}

	public void done() {
		/*
		for(Map.Entry<String,List<Ref>> e : references.entrySet()) {		
			if (e.getValue() != DEFINED) {
				System.out.println("Dangling references to id "+e.getKey());
			}
		}
		*/
	}	
}
