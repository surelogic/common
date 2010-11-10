package com.surelogic.common.refactor;

import java.util.*;

public class JavaDeclInfo {
	final JavaDeclInfo parent;
	final DeclKind kind;
	private final Map<String,String> attributes = new HashMap<String, String>(1);
	
	JavaDeclInfo(AbstractJavaDeclaration decl, JavaDeclInfo parent, String key, String value) {
		this.parent = parent;
		this.kind = decl.getKind();
		attributes.put(key, value);
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
			return new Method((TypeContext) parent, attributes.get(Method.METHOD), params);
		case PARAM:
			return new MethodParameter((Method) parent, 
					Integer.valueOf(attributes.get(MethodParameter.PARAM_NUM))); 
		case TYPE_CONTEXT:
			final String name = attributes.get(IJavaDeclaration.NAME);
			if (parent instanceof TypeContext) {
				return new TypeContext((TypeContext) parent, name);
			} else {
				return new TypeContext((Method) parent, name);
			}
		}
		throw new IllegalStateException("Unexpected: "+kind);
	}

	private String[] separateByColons(String s) {
		return s.split(":");
	}
}
