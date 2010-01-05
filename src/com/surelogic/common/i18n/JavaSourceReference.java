package com.surelogic.common.i18n;

/**
 * A class that stores a reference to a Java source reference. The project name
 * and line number are optional. The package name and type name are required.
 * This is a value object.
 */
public final class JavaSourceReference {

	/**
	 * Optional, may be null.
	 */
	private final String f_projectName;

	/**
	 * non-null
	 */
	private final String f_packageName;

	/**
	 * non-null
	 */
	private final String f_typeName;

	/**
	 * Must be non-negative. 0 indicates that the line number is not known.
	 */
	private final int f_lineNumber;

	public JavaSourceReference(String projectName, String packageName,
			String typeName, int lineNumber) {
		f_projectName = projectName;
		if (packageName == null)
			throw new IllegalArgumentException(I18N.err(44, "packageName"));
		f_packageName = packageName;
		if (typeName == null)
			throw new IllegalArgumentException(I18N.err(44, "typeName"));
		f_typeName = typeName;
		if (lineNumber < 0)
			lineNumber = 0;
		f_lineNumber = lineNumber;
	}

	public JavaSourceReference(String packageName, String typeName,
			int lineNumber) {
		this(null, packageName, typeName, lineNumber);
	}

	public JavaSourceReference(String packageName, String typeName) {
		this(packageName, typeName, 0);
	}

	public String getProjectName() {
		return f_projectName;
	}

	public String getPackageName() {
		return f_packageName;
	}

	public String getTypeName() {
		return f_typeName;
	}

	public int getLineNumber() {
		return f_lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + f_lineNumber;
		result = prime * result
				+ ((f_packageName == null) ? 0 : f_packageName.hashCode());
		result = prime * result
				+ ((f_projectName == null) ? 0 : f_projectName.hashCode());
		result = prime * result
				+ ((f_typeName == null) ? 0 : f_typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaSourceReference other = (JavaSourceReference) obj;
		if (f_lineNumber != other.f_lineNumber)
			return false;
		if (f_packageName == null) {
			if (other.f_packageName != null)
				return false;
		} else if (!f_packageName.equals(other.f_packageName))
			return false;
		if (f_projectName == null) {
			if (other.f_projectName != null)
				return false;
		} else if (!f_projectName.equals(other.f_projectName))
			return false;
		if (f_typeName == null) {
			if (other.f_typeName != null)
				return false;
		} else if (!f_typeName.equals(other.f_typeName))
			return false;
		return true;
	}

	/**
	 * Returns an <i>n</i>-tuple containing the contents of this object. The
	 * result is of the form
	 * (<i>projectName</i>,<i>packageName</i>,</i>typeName<
	 * /i>,<i>lineNumber</i>).
	 * 
	 * 
	 * If the project name is {@code null} it is not included. If the line
	 * number is 0 (meaning unknown) it is not included.
	 * 
	 * @return
	 */
	public String toStringCanonical() {
		final StringBuilder b = new StringBuilder();
		b.append('(');
		final String projectName = getProjectName();
		if (projectName != null)
			b.append(projectName).append(',');
		b.append(getPackageName()).append(',');
		b.append(getTypeName());
		final int lineNumber = getLineNumber();
		if (lineNumber > 0)
			b.append(',').append(getLineNumber());
		b.append(')');
		return b.toString();
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + toStringCanonical();
	}
}
