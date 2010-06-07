package com.surelogic.common.license;

import com.surelogic.common.OString;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;

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
		/* => "A.Real.Java.Puzz1er" */
		super(
				new OString(new long[] { 0x7191EF595FDF3683L,
						0x6C3EFC4DC1016796L, 0xF6EB4C62D9AEBBC4L,
						0xB858B835D85DFAC6L }).toString());
	}
}
