package com.surelogic.common.license;

import javax.security.auth.x500.X500Principal;

import com.surelogic.common.i18n.I18N;

public final class SLLicenseUtility {

	public static final String ALL_TOOL_SUBJECT = "SureLogic All-Tool License";
	public static final String FLASHLIGHT_SUBJECT = "SureLogic Flashlight License";

	/**
	 * Creates an X500 principal for SureLogic.
	 * 
	 * @return an X500 principal for SureLogic.
	 */
	public static X500Principal getSureLogicX500Principal() {
		final X500Principal result = new X500Principal(
				"CN=SureLogic\\, Inc., OU=http://www.surelogic.com, L=Pittsburgh, ST=Pennsylvania, C=US");
		return result;
	}

	/**
	 * Creates an X500 principal for the passed <tt>CN</tt> name. The name is
	 * escaped to try to avoid parsing errors when the {@link X500Principal}
	 * instance is constructed.
	 * 
	 * @param cn
	 *            the <tt>CN</tt> value for the X500 principal. Should be
	 *            something like a name or a company name.
	 * @return an 500 principal.
	 * @throws IllegalArgumentException
	 *             if <tt>CN</tt> is null or the empty string or if after
	 *             escaping <tt>CN</tt> is not able to be parsed by the
	 *             constructor {@link X500Principal#X500Principal(String)}.
	 */
	public static X500Principal getX500PrincipalFor(final String cn) {
		if (cn == null)
			throw new IllegalArgumentException(I18N.err(44, "cn"));
		if ("".equals(cn))
			throw new IllegalArgumentException(I18N.err(139, "cn"));
		final StringBuilder b = new StringBuilder(cn);
		remove("\\", b);
		escape(",", b);
		escape(";", b);
		b.insert(0, "CN=");
		return new X500Principal(b.toString());
	}

	private static void escape(final String value, final StringBuilder b) {
		int index = 0;
		while (true) {
			index = b.indexOf(value, index);
			if (index == -1)
				break;
			b.insert(index, "\\");
			index = index + 1 + value.length();
		}
	}

	private static void remove(final String value, final StringBuilder b) {
		int index = 0;
		while (true) {
			index = b.indexOf(value, index);
			if (index == -1)
				break;
			b.replace(index, index + 1, "");
		}
	}

	private SLLicenseUtility() {
		// no instances
	}
}
