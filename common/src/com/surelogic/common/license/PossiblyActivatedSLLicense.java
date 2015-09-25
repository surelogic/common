package com.surelogic.common.license;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Represents a possibly installed license. A license is considered installed if
 * this class aggregates both of the following:
 * <ul>
 * <li>A signed license
 * <li>A signed license net check
 * </ul>
 * Note that being installed is a necessary but not sufficient condition for a
 * license to be considered valid.
 */
public final class PossiblyActivatedSLLicense {

  /**
   * A license and the signed hex string that demonstrates that the license was
   * created by SureLogic.
   */
  @NonNull
  private final SignedSLLicense f_license;

  /**
   * Gets the signed license associated with this possibly installed license.
   * 
   * @return the non-<tt>null</tt> signed license.
   */
  @NonNull
  public SignedSLLicense getSignedSLLicense() {
    return f_license;
  }

  /**
   * A license net check and the signed hex string that demonstrates that the
   * license net check was created by SureLogic. May be {@code null} if the
   * {@link #f_license} has not been net checked.
   */
  @Nullable
  private final SignedSLLicenseNetCheck f_licenseNetCheck;

  /**
   * Gets the signed license net check associated with this possibly installed
   * license.
   * 
   * @return the signed license net check, or {@code null} if there is no net
   *         check.
   */
  @Nullable
  public SignedSLLicenseNetCheck getSignedSLLicenseNetCheck() {
    return f_licenseNetCheck;
  }

  /**
   * Flags if this license is activated. Activation is indicated by the
   * existence of a {@link SignedSLLicenseNetCheck} object.
   * 
   * @return {@code true} if this license has been activated, {@code false}
   *         otherwise.
   */
  public boolean isActivated() {
    return f_licenseNetCheck != null;
  }

  /**
   * A textual explanation of the activation status of this license. The result
   * is intended for use in the user interface.
   * 
   * @return a textual explanation of the activation status of this license.
   */
  @NonNull
  public String getActivatedExplanation() {
    final StringBuilder b = new StringBuilder();
    if (isActivated()) {
      b.append(SLUtility.YES);
    } else {
      b.append(SLUtility.NO);
      final SLLicense license = getSignedSLLicense().getLicense();
      final boolean deadline = license.getInstallBeforeDate() != null && license.getType() != SLLicenseType.PERPETUAL;
      if (deadline) {
        final String date = SLUtility.toStringHumanDay(license.getInstallBeforeDate());
        b.append(" (activate before ").append(date).append(")");
      }
    }
    return b.toString();
  }

  /**
   * Flags if this license is expired. The type of the license is not taken into
   * consideration just the date in the license net check. This method returns
   * {@code false} if the license has not been activated.
   * 
   * @return {@code true} if this license has expired, {@code false} otherwise.
   */
  public boolean isExpired() {
    return isExpiredOn(new Date());
  }

  /**
   * Flags if this license is within a week of expiration. The type of the
   * license is not taken into consideration just the date in the license net
   * check. This method returns {@code false} if the license has not been
   * activated.
   * 
   * @return {@code true} if this license is close to expiration, {@code false}
   *         otherwise.
   */
  public boolean isCloseToBeingExpired() {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 7);
    final Date inAWeek = cal.getTime();
    return isExpiredOn(inAWeek);
  }

  /**
   * Flags if the this license is expired on the passed date. The type of the
   * license is not taken into consideration just the date in the license net
   * check. This method returns {@code false} if the license has not been
   * activated.
   * 
   * @param date
   *          a date.
   * @return {@code true} if this license is expired on the passed date,
   *         {@code false} otherwise.
   * 
   * @throws IllegalArgumentException
   *           if <tt>date</tt> is {@code null}.
   */
  public boolean isExpiredOn(Date date) {
    if (isActivated()) {
      final Date deadline = f_licenseNetCheck.getLicenseNetCheck().getDate();
      final boolean expired = date.after(deadline);
      return expired;
    } else {
      return false;
    }
  }

  /**
   * A textual explanation of the expiration status of this license. The result
   * is intended for use in the user interface.
   * 
   * @return a textual explanation of the expiration status of this license.
   */
  @NonNull
  public String getExpirationExplanation() {
    final StringBuilder b = new StringBuilder();
    final SLLicenseType type = getSignedSLLicense().getLicense().getType();

    if (isActivated()) {
      final String date = SLUtility.toStringHumanDay(getSignedSLLicenseNetCheck().getLicenseNetCheck().getDate());
      if (isExpired()) {
        b.append(SLUtility.YES);
        if (type == SLLicenseType.PERPETUAL) {
          b.append(" (renew now)");
        }
      } else {
        b.append(SLUtility.NO).append(" (expires ").append(date).append(")");
      }
    }
    return b.toString();
  }

  /**
   * Flags if this license is past its installation deadline if it has one. if
   * checking a perpetual license this method always returns {@code false}.
   * 
   * @return {@code true} if this license is past its installation deadline,
   *         {@code false} otherwise (does not have a deadline or this license
   *         is perpetual).
   */
  public boolean isPastInstallBeforeDate() {
    final SLLicense license = f_license.getLicense();
    final Date deadline = license.getInstallBeforeDate();
    if (deadline != null) {
      final Date now = new Date();
      if (license.getType() != SLLicenseType.PERPETUAL) {
        final boolean pastDeadline = now.after(deadline);
        return pastDeadline;
      }
    }
    return false;
  }

  /**
   * Flags if this license allows the use of the passed product on the machine
   * with the passed MAC addresses.
   * 
   * @param product
   *          a SureLogic product. Cannot be <tt>null</tt>.
   * @param macAddresses
   *          the set of MAC addresses on the machine trying to use the license.
   *          May be empty or {@code null} to indicate no MAC addresses.
   * @return {@code true} if this license allows the use of <tt>product</tt> on
   *         the machine with the passed MAC addresses, {@code false} otherwise.
   * 
   * @throw IllegalArgumentException if <tt>product</tt> is {@code null}.
   */
  public boolean licensesUseOf(@NonNull final SLLicenseProduct product, @Nullable Iterable<String> macAddresses) {
    if (product == null)
      throw new IllegalArgumentException(I18N.err(44, "product"));
    if (!product.isALicensedProduct())
      throw new IllegalArgumentException(I18N.err(356, product));

    if (!isActivated())
      return false;

    if (macAddresses == null)
      macAddresses = ImmutableSet.of(); // null means empty
    if (!getSignedSLLicenseNetCheck().getLicenseNetCheck().containsAtLeastOneMacAddress(macAddresses))
      return false;

    final SLLicense license = f_license.getLicense();
    final SLLicenseType type = license.getType();

    /*
     * USE licenses expire on the date specified.
     */
    if (isExpired() && type == SLLicenseType.USE)
      return false;

    /*
     * SUPPORT and PERPETUAL licenses allow use of versions released before
     * their expiration date or renewal deadline, respectively.
     */
    if (type == SLLicenseType.SUPPORT || type == SLLicenseType.PERPETUAL) {
      @Nullable
      final Date releaseDate = SLLicenseUtility.getToolReleaseDateOrNull();
      if (releaseDate != null && isExpiredOn(releaseDate))
        return false;
    }

    if (license.getProduct().includes(product))
      return true;
    else
      return false;
  }

  /**
   * Constructs a new instance from the passed signed license and signed license
   * net check.
   * 
   * @param license
   *          a non-<tt>null</tt> signed license.
   * @param licenseNetCheck
   *          a signed license net check, or {@code null} if there is no net
   *          check.
   */
  public PossiblyActivatedSLLicense(@NonNull SignedSLLicense license, @Nullable SignedSLLicenseNetCheck licenseNetCheck) {
    if (license == null)
      throw new IllegalArgumentException(I18N.err(44, "license"));
    f_license = license;
    f_licenseNetCheck = licenseNetCheck;

    /*
     * Validate the two objects: If the license net check is not null then
     * ensure that the two identifiers are the same.
     */
    if (f_licenseNetCheck != null) {
      final UUID liUuid = f_license.getLicense().getUuid();
      final UUID ncUuid = f_licenseNetCheck.getLicenseNetCheck().getUuid();
      final boolean differentUuids = !liUuid.equals(ncUuid);
      if (differentUuids)
        throw new IllegalStateException(I18N.err(189, liUuid.toString(), ncUuid.toString()));
    }
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append(this.getClass().toString());
    if (isActivated()) {
      b.append(" : installed\n");
      b.append(getSignedSLLicense().toString());
      b.append(getSignedSLLicenseNetCheck().toString());
    } else {
      b.append(" : not installed\n");
      b.append(getSignedSLLicense().toString());
    }
    return b.toString();
  }
}
