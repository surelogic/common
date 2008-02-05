package com.surelogic.common.jdbc;

/**
 * An exception that indicates that the version of the database schema is newer
 * than the version that the code expects. This exception helps us to support
 * RfR requirement 3.1.15:
 * <p>
 * Will you code your plug-ins so that new versions will not prevent older
 * versions from becoming active again if the user chooses to revert to a
 * previous level of code? (Yes is required.)
 */
public final class FutureDatabaseException extends Exception {

	private int f_codeVersion;

	public int getCodeVersion() {
		return f_codeVersion;
	}

	private int f_schemaVersion;

	public int getSchemaVersion() {
		return f_schemaVersion;
	}

	public FutureDatabaseException(int codeVersion, int schemaVersion) {
		f_codeVersion = codeVersion;
		f_schemaVersion = schemaVersion;
	}

	private static final long serialVersionUID = 6446502447505736644L;
}
