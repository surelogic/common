package com.surelogic.common.license;

import javax.security.auth.x500.X500Principal;

public final class SLLicenseUtility {

	public static X500Principal getSureLogicX500Principal() {
		final X500Principal result = new X500Principal(
				"CN=SureLogic\\, Inc., OU=http://www.surelogic.com, L=Pittsburgh, ST=Pennsylvania, C=US");
		return result;
	}

	private SLLicenseUtility() {
		// no instances
	}
}
