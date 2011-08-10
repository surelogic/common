package com.surelogic.common.refactor;

import java.util.*;

import org.xml.sax.Attributes;

import com.surelogic.common.xml.Entity;

public class JavaDeclInfo extends Entity implements IJavaDeclInfoClient {	
	public static final String INFO_KIND = "info-kind";

	// Indicates what the info is from / used for 
	public static final String PARENT = "parent";

	static {
		for(DeclKind k : DeclKind.values()) {
			internString(k.toString());
		}
		internString(PARENT);
	}
	
	JavaDeclInfo parent;
	final DeclKind kind;
	
	JavaDeclInfo(AbstractJavaDeclaration decl, JavaDeclInfo parent, String key, String value) {
		super(decl.getKind().toString(), Collections.<String,String>emptyMap());
		this.parent = parent;
		this.kind = decl.getKind();
		attributes.put(key, value);
		addRef(parent);
	}

	public JavaDeclInfo(String name, Attributes a) {
		super(name, a);
		parent = null;
		kind = DeclKind.valueOf(getValue(a, INFO_KIND));
		if (kind.equals(DeclKind.TYPE_CONTEXT)) {
			maybeReplace(IJavaDeclaration.NAME);
		}
	}

	public JavaDeclInfo getParent() {
		return parent;
	}

	public DeclKind getKind() {
		return kind;
	}
	
	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public IJavaDeclaration makeDecl() {
		final IJavaDeclaration parent = this.parent != null ? this.parent.makeDecl() : null;
		switch (kind) {
		case FIELD:
			return new Field((TypeContext) parent, attributes.get(Field.FIELD));
		case METHOD:
			String[] params = separateByColons(attributes.get(Method.PARAMS));
			return new Method((TypeContext) parent, attributes.get(Method.METHOD), params, 
					          "true".equals(attributes.get(Method.IS_IMPLICIT)));
		case PARAM:
			return new MethodParameter((Method) parent, 
					Integer.valueOf(attributes.get(MethodParameter.PARAM_NUM))); 
		case TYPE_CONTEXT:
			final String name = attributes.get(IJavaDeclaration.NAME);
			
			if (parent == null) {
				return new TypeContext(name);
			} else if (parent instanceof TypeContext) {
				return new TypeContext((TypeContext) parent, name);
			} else {
				return new TypeContext((Method) parent, name);
			}
		}
		throw new IllegalStateException("Unexpected: "+kind);
	}

	private final String[] noStrings = new String[0];
	
	private String[] separateByColons(String s) {
		if ("".equals(s)) {
			return noStrings;
		}
		return s.split(":");
	}

	public void addInfo(JavaDeclInfo info) {
		// TODO check flavor?
		parent = info;
	}
}
