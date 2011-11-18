package com.surelogic.common.refactor;

/**
 * Represents a (possibly nested) type in a compilation unit
 * 
 * @author nathan
 * 
 */
public class TypeContext extends AbstractJavaDeclaration {	
	private final TypeContext parent;
	private final String name;
	private final Method method;

	public TypeContext(final TypeContext parent, final String name) {
		if (parent == null) {
			throw new IllegalArgumentException(
					"If parent is null, use other constructor.");
		}
		this.method = null;
		this.name = name;
		this.parent = parent;
	}

	public TypeContext(final String name) {
		this.method = null;
		this.parent = null;
		this.name = name;
	}

	public TypeContext(final Method m, final String id) {
		this.parent = m.getTypeContext();
		this.method = m;
		this.name = id;
	}
	
	public TypeContext(final Field f, final String id) {
		this.parent = f.getTypeContext();
		this.method = null; // TODO ignoring field
		this.name = id;
	}

	public TypeContext getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public Method getMethod() {
		return method;
	}

	public String forSyntax() {
		return name;
	}

	public TypeContext getTypeContext() {
		return this;
	}

	@Override
	public String toString() {
		return "TypeContext [name=" + name + ", method=" + method + ", parent="
				+ parent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (method == null ? 0 : method.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (parent == null ? 0 : parent.hashCode());
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
		final TypeContext other = (TypeContext) obj;
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		return true;
	}

	public DeclKind getKind() {
		return DeclKind.TYPE_CONTEXT;
	}
	
	public JavaDeclInfo snapshot() {
		final JavaDeclInfo parent;
		if (this.parent != null) {
			parent = this.parent.snapshot();
		} 
		else if (method != null) {
			parent = method.snapshot();
		}
		else  {
			parent = null;
		}
		return new JavaDeclInfo(this, parent, NAME, name);
	}
}