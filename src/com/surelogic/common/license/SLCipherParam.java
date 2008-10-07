package com.surelogic.common.license;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.util.ObfuscatedString;

/**
 * The SureLogic implementation of the {@link CipherParam} interface. This
 * implementation may be used for all products.
 */
final class SLCipherParam extends DefaultCipherParam {

	private static final CipherParam INSTANCE = new SLCipherParam();

	static CipherParam getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates a new instance using an obfuscated password to be returned by
	 * {@link #getKeyPwd()}.
	 */
	private SLCipherParam() {
		/* => "A.Real.Java.Puzzler" */
		super(
				new ObfuscatedString(new long[] { 0xF0D8BA0005A49E8EL,
						0x99BAA9DBC1976481L, 0x9BF65FA74D624214L,
						0x3FAF3665A81A5A10L }).toString());
	}
}
