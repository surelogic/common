package com.surelogic.common.refactor;

import java.util.Arrays;

/**
 * A method signature in a Java type.
 * 
 * @author nathan
 * 
 */
public class Method extends AbstractJavaDeclaration {
	public static final String METHOD = "method";
	public static final String PARAMS = "params";
	public static final String IS_IMPLICIT = "is-implicit";
	
	private final TypeContext type;
	private final String method;
	private final String[] params;
	private final boolean isImplicit;
	
	/**
	 * Construct a new method with the given type and param classes
	 * 
	 * @param type
	 * @param params
	 *            the class name of the parameter, or an empty array if there
	 *            are no parameters
	 */
	public Method(final TypeContext type, final String method,
			final String[] params, boolean isImplicit) {
		this.type = type;
		this.method = method;
		this.params = params;
		this.isImplicit = isImplicit;
	}

	@Override
	public boolean isImplicit() {
		return isImplicit;
	}
	
	/**
	 * The name of the method
	 * 
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * An array of the parameter types, not including {@code this}
	 * 
	 * @return
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * The type this method signature is declared in.
	 * 
	 * @return
	 */
	public TypeContext getTypeContext() {
		return type;
	}

	public String getSignature() {
		final StringBuilder b = new StringBuilder();
		makeSignature(b);
		return b.toString();
	}
	
	private void makeSignature(StringBuilder b) {
		if (method.equals(type.getName())) {
			b.append("new");
		} else {
			b.append(method);
		}
		b.append('(');
		for (final String s : params) {
			b.append(s);
			b.append(',');
		}
		if (params.length > 0) {
			b.setLength(b.length() - 1);
		}
		b.append(')');
	}
	
	public String forSyntax() {
		final StringBuilder b = new StringBuilder();
		makeSignature(b);
		b.append(" in ");
		b.append(type.forSyntax());
		return b.toString();
	}

	@Override
	public String toString() {
		return "Method [method=" + method + ", params="
				+ Arrays.toString(params) + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (method == null ? 0 : method.hashCode());
		result = prime * result + Arrays.hashCode(params);
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Method other = (Method) obj;
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (!Arrays.equals(params, other.params)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	public DeclKind getKind() {
		return DeclKind.METHOD;
	}
	
	public JavaDeclInfo snapshot() {
		final JavaDeclInfo info = new JavaDeclInfo(this, type.snapshot(), METHOD, method);
		info.addAttribute(PARAMS, addColons(params));
		info.addAttribute(IS_IMPLICIT, Boolean.toString(isImplicit));
		return info;
	}
}
