package com.surelogic.common.license;

import java.util.prefs.Preferences;

import com.surelogic.common.i18n.I18N;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseParam;

public class SLLicenseParam implements LicenseParam {

	private final KeyStoreParam f_keyStoreParam;
	private final String f_subject;

	/**
	 * Creates a new instance using {@link SLCipherParam} and the passed
	 * information. The preferences path is generated from the passed subject
	 * within <tt>/com/surelogic/licenses/</tt>.
	 * <p>
	 * It is critical that the subject remain consistent across license
	 * generation, installation, and verification.
	 * <p>
	 * Use this constructor for license generation, use
	 * {@link #SLLicenseParam(String)} for license installation and
	 * verification.
	 * 
	 * @param subject
	 *            the non-null subject of the license usable in the user
	 *            interface. Examples: <tt>"Flashlight"</tt>, <tt>"Sierra"</tt>,
	 *            <tt>"All SureLogic Tools"</tt>. Ensure that this string
	 *            doesn't contain any <tt>'/'</tt> characters.
	 * @param keyStoreParam
	 *            {@code null} to use an instance of
	 *            {@link SLPublicKeyStoreParam} or a reference to another
	 *            implementation (e.g., a private key store).
	 */
	public SLLicenseParam(final String subject,
			final KeyStoreParam keyStoreParam) {
		if (keyStoreParam == null) {
			f_keyStoreParam = SLPublicKeyStoreParam.getInstance();
		} else {
			f_keyStoreParam = keyStoreParam;
		}
		if (subject == null)
			throw new IllegalArgumentException(I18N.err(44, "subject"));
		f_subject = subject;
	}

	/**
	 * Creates a new instance using {@link SLCipherParam} and the passed
	 * information. The preferences path is generated from the passed subject
	 * within <tt>/com/surelogic/licenses/</tt>.
	 * <p>
	 * It is critical that the subject remain consistent across license
	 * generation, installation, and verification.
	 * <p>
	 * Use this constructor for license installation and verification, use
	 * {@link #SLLicenseParam(String, KeyStoreParam)} for license generation.
	 * 
	 * @param subject
	 *            the non-null subject of the license usable in the user
	 *            interface. Examples: <tt>"Flashlight"</tt>, <tt>"Sierra"</tt>,
	 *            <tt>"All SureLogic Tools"</tt>. Ensure that this string
	 *            doesn't contain any <tt>'/'</tt> characters.
	 */
	public SLLicenseParam(final String subject) {
		this(subject, null);
	}

	public CipherParam getCipherParam() {
		return SLCipherParam.getInstance();
	}

	public KeyStoreParam getKeyStoreParam() {
		return f_keyStoreParam;
	}

	public Preferences getPreferences() {
		return Preferences.userRoot().node(
				"/com/surelogic/licenses/" + f_subject);
	}

	public String getSubject() {
		return f_subject;
	}
}
