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
		X500Principal p = SLLicenseUtility.getX500PrincipalFor(name);
		assertNotNull(p);
		assertEquals(SLLicenseUtility.getNameFrom(p), name);
		UUID uuid = UUID.fromString(SLLicenseUtility.getUUIDFrom(p));
		assertNotNull(uuid);

		name = ",,::;;Co,,";
		p = SLLicenseUtility.getX500PrincipalFor(name);
		assertNotNull(p);
		assertEquals(SLLicenseUtility.getNameFrom(p), name);
		uuid = UUID.fromString(SLLicenseUtility.getUUIDFrom(p));
		assertNotNull(uuid);
	}

}
