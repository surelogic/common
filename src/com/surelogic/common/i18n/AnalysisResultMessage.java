package com.surelogic.common.i18n;

/**
 * Represents an analysis result message. The message can be output in a human
 * readable form as well as in a canonical form.
 * <p>
 * The {@code getInstance} methods are used to construct methods and the result
 * number matches those in the <tt>SureLogicResults.properties</tt> file defined
 * in this package.
 * 
 * @see I18N
 * @see JavaSourceReference
 */
public final class AnalysisResultMessage {
	public static final Object[] noArgs = new Object[0];
	
	/**
	 * This number must exist in the <tt>SureLogicResults.properties</tt> file
	 * defined in this package.
	 */
	private final int f_number;

	/**
	 * non-null, but may be empty
	 */
	private final Object[] f_args;

	/**
	 * non-null
	 */
	private final JavaSourceReference f_srcRef;

	private AnalysisResultMessage(JavaSourceReference srcRef, int number,
			Object... args) {
		f_number = number;
		f_args = (args.length > 0) ? args : noArgs;
		if (srcRef == null)
			throw new IllegalArgumentException(I18N.err(44, "srcRef"));
		f_srcRef = srcRef;
	}

	public static AnalysisResultMessage getInstance(JavaSourceReference srcRef,
			int number) {
		// TODO cache the formatted message?
		I18N.res(number); // toss result, but ensure the call works
		return new AnalysisResultMessage(srcRef, number, noArgs);
	}

	public static AnalysisResultMessage getInstance(JavaSourceReference srcRef,
			int number, Object... args) {
		// TODO cache the formatted message?
		I18N.res(number, args); // toss result, but ensure the call works
		return new AnalysisResultMessage(srcRef, number, args);
	}

	public String getResultString() {
		return f_args.length == 0 ? I18N.res(f_number) : I18N.res(f_number,
				f_args);
	}

	public String getResultStringCanonical() {
		return f_args.length == 0 ? I18N.resc(f_number) : I18N.resc(f_number,
				f_args);
	}

	public JavaSourceReference getSrcRef() {
		return f_srcRef;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(getResultString());
		b.append(' ');
		b.append(f_srcRef.toStringMessage());
		return b.toString();
	}

	public String toStringCanonical() {
		final StringBuilder b = new StringBuilder();
		b.append(getResultStringCanonical());
		b.append('@');
		b.append(f_srcRef.toStringCanonical());
		return b.toString();
	}

	public boolean sameAs(int num, Object[] args) {
		if (num == f_number) {
			if (args == null) {
				return f_args == null;
			}
			if (args.length != f_args.length) {
				return false;
			}
			for(int i=0; i<args.length; i++) {
				if (args[i] != null) {
					if (!args[i].equals(f_args[i])) {
						return false;
					}
				} else if (f_args[i] != null){
					return false; // args[i] is null, so different
				}
			}
			return true;
		}
		return false;
	}
}
