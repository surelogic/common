package com.surelogic.common.license;

import de.schlichtherle.license.AbstractKeyStoreParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.util.ObfuscatedString;

/**
 * The SureLogic implementation of {@link KeyStoreParam} accessing the
 * <tt>SureLogicPublic.store</tt> file located in this package.
 */
final class SLPublicKeyStoreParam extends AbstractKeyStoreParam {

	private static final KeyStoreParam INSTANCE = new SLPublicKeyStoreParam();

	static KeyStoreParam getInstance() {
		return INSTANCE;
	}

	private SLPublicKeyStoreParam() {
		super(SLPublicKeyStoreParam.class,
				"/com/surelogic/common/license/SureLogicPublic.store");
	}

	public String getAlias() {
		/* => "surelogic-public" */
		return new ObfuscatedString(new long[] { 0xA124AAE729161A7CL,
				0xB2DC5D6C3CF0E724L, 0x3F955B0AB54B6586L }).toString();
	}

	public String getStorePwd() {
		/* => "public" */
		return new ObfuscatedString(new long[] { 0x4BB7D4210BCC59B2L,
				0xB190D812BA270CBL }).toString();
	}

	public String getKeyPwd() {
		/*
		 * We never want to unlock a private key.
		 */
		return null;
	}
}
