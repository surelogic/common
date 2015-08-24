package com.surelogic.common.license;

import java.security.PrivateKey;
import java.security.PublicKey;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Encapsulates a license and the signed hex string that demonstrates that the
 * license was created by SureLogic.
 */
public final class SignedSLLicense {

  private final SLLicense f_license;

  public SLLicense getLicense() {
    return f_license;
  }

  private final String f_signedHexString;

  public String getSignedHexString() {
    return f_signedHexString;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append(this.getClass().toString());
    b.append(" : (signed hex string) ");
    b.append(getSignedHexString());
    b.append('\n');
    b.append(getLicense().toString());
    return b.toString();
  }

  private SignedSLLicense(final SLLicense license, final String signedHexString) {
    f_license = license;
    f_signedHexString = signedHexString;
  }

  /**
   * Creates a signed license object from a passed hex encoded string using the
   * public key obtained from {@link SLUtility#getPublicKey()} to check that the
   * strings digital signature.
   * 
   * @param signedHexString
   *          a signed hex string that demonstrates that the license was created
   *          by SureLogic.
   * @return a signed license object.
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  public static SignedSLLicense getInstance(String signedHexString) {
    return getInstance(signedHexString, SLUtility.getPublicKey());
  }

  /**
   * Creates a signed license object from a passed hex encoded string using a
   * public key to check that the strings digital signature.
   * 
   * @param signedHexString
   *          a signed hex string that demonstrates that the license was created
   *          by the private key corresponding to <tt>key</tt>.
   * @param key
   *          a public key.
   * @return a signed license object.
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  public static SignedSLLicense getInstance(String signedHexString, PublicKey key) {
    if (signedHexString == null)
      throw new IllegalArgumentException(I18N.err(44, "signedHexString"));
    if (key == null)
      throw new IllegalArgumentException(I18N.err(44, "key"));
    SLLicense license = SLLicensePersistence.toLicense(signedHexString, key);
    if (license == null)
      throw new IllegalArgumentException(I18N.err(204, signedHexString));
    return new SignedSLLicense(license, signedHexString);
  }

  /**
   * Creates a signed license object from the passed license object using a
   * private key digitally sign the data.
   * 
   * @param license
   *          a license object.
   * @param key
   *          a private key to sign the license contents with.
   * @return a signed license object.
   * @throws IllegalArgumentException
   *           if any parameter to this method is {@code null}.
   */
  public static SignedSLLicense getInstance(SLLicense license, PrivateKey key) {
    if (license == null)
      throw new IllegalArgumentException(I18N.err(44, "license"));
    if (key == null)
      throw new IllegalArgumentException(I18N.err(44, "key"));
    final String signedHexString = SLLicensePersistence.toSignedHexString(license, key, false);
    return new SignedSLLicense(license, signedHexString);
  }
}
