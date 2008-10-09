package com.surelogic.common.license;

import java.util.UUID;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

public class TestSLLicenseUtility extends TestCase {

	public void testX500PrincipalSureLogic() {
		X500Principal p = SLLicenseUtility.getSureLogicX500Principal();
		assertNotNull(p);
		assertTrue(p.getName().indexOf("SureLogic") != -1);
	}

	public void testX500PrincipalFor() {
		String name = "SureLogic, Inc.";
		X500Principal p = SLLicenseUtility.getX500PrincipalFor(name, true);
		System.out.println(p.toString());
		assertNotNull(p);
		assertEquals(SLLicenseUtility.getIssuedToFrom(p), name);
		UUID uuid = UUID.fromString(SLLicenseUtility.getLicenseIdFrom(p));
		assertNotNull(uuid);
		assertTrue(SLLicenseUtility.getPerformNetCheckFrom(p));

		name = ",,::;;Co,,";
		p = SLLicenseUtility.getX500PrincipalFor(name, false);
		assertNotNull(p);
		assertEquals(SLLicenseUtility.getIssuedToFrom(p), name);
		uuid = UUID.fromString(SLLicenseUtility.getLicenseIdFrom(p));
		assertNotNull(uuid);
		assertFalse(SLLicenseUtility.getPerformNetCheckFrom(p));
	}

}
