package com.surelogic.common.license;

import java.util.Date;
import java.util.UUID;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Unique;
import com.surelogic.Vouch;
import com.surelogic.common.i18n.I18N;

/**
 * A class that represents an immutable SureLogic license.
 * 
 * @see SLLicenseType
 */
@Immutable
public final class SLLicense {

  /**
   * The identify of this license. May not be <tt>null</tt>.
   */
  @NonNull
  private final UUID f_uuid;

  /**
   * Gets the {@link UUID} that identifies this license.
   * 
   * @return the non-<tt>null</tt> {@link UUID} that identifies this license.
   */
  @NonNull
  public UUID getUuid() {
    return f_uuid;
  }

  /**
   * The name of the license holder. May not be <tt>null</tt>.
   */
  @NonNull
  private final String f_holder;

  /**
   * Gets the name of the license holder.
   * 
   * @return the non-<tt>null</tt> name of the license holder.
   */
  @NonNull
  public String getHolder() {
    return f_holder;
  }

  /**
   * The email of the license holder.
   */
  @Nullable
  private final String f_email;

  /**
   * Gets the email of the license holder.
   * 
   * @return the email of the license holder, or {@code null} if unknown.
   */
  @Nullable
  public String getEmail() {
    return f_email;
  }

  /**
   * The company of the license holder.
   */
  @Nullable
  private final String f_company;

  /**
   * Gets the company provided by the license holder.
   * 
   * @return the company provided by the license holder, or {@code null} if
   *         none.
   */
  @Nullable
  public String getCompany() {
    return f_company;
  }

  /**
   * The name of the product being licensed. May not be <tt>null</tt>.
   */
  @NonNull
  private final SLLicenseProduct f_product;

  /**
   * Gets the product being licensed.
   * 
   * @return the non-<tt>null</tt> product being licensed.
   */
  @NonNull
  public SLLicenseProduct getProduct() {
    return f_product;
  }

  /**
   * The license duration in days from installation until expiration or renewal.
   * This value must be greater than one.
   */
  private final int f_durationInDays;

  /**
   * Gets the license duration in days from installation until expiration or
   * renewal. This value must be greater than one.
   * 
   * @return the license duration in days from installation until expiration or
   *         renewal.
   */
  public int getDurationInDays() {
    return f_durationInDays;
  }

  /**
   * An install before date. Installations and activations after this date will
   * fail. May not be <tt>null</tt>.
   */
  @Vouch("Immutable")
  @Unique
  @Nullable
  private final Date f_installBeforeDate;

  /**
   * Gets installation deadline, or install before date, for this license.
   * Installations and activations after this date will fail.
   * 
   * @return the installation deadline for this license, or <tt>null</tt> to
   *         indicate no deadline.
   */
  @Nullable
  public Date getInstallBeforeDate() {
    if (f_installBeforeDate == null)
      return null;
    else
      return new Date(f_installBeforeDate.getTime());
  }

  /**
   * The type of this license. May not be <tt>null</tt>.
   */
  @NonNull
  private final SLLicenseType f_type;

  /**
   * Gets the type of this license.
   * 
   * @return the non-<tt>null</tt> type of this license.
   */
  @NonNull
  public SLLicenseType getType() {
    return f_type;
  }

  /**
   * The number of active installations allowed before an attempted installation
   * fails. This value must be greater than zero.
   */
  private final int f_maxActive;

  /**
   * Gets the number of active installations allowed before an attempted
   * installation fails. This value must be greater than zero.
   * 
   * @return the number of active installations allowed before an attempted
   *         installation fails.
   */
  public int getMaxActive() {
    return f_maxActive;
  }

  /**
   * Flags if a net check is required for installation of this license. A value
   * of {@code true} indicates yes, a value of {@code false} indicates no.
   * <p>
   * A net check is required for all licenses of type
   * {@link SLLicenseType#PERPETUAL}.
   */
  private final boolean f_performNetCheck;

  /**
   * Flags if a net check is required for installation of this license.
   * 
   * @return {@code true} if a net check is required, {@code false} otherwise.
   */
  public boolean performNetCheck() {
    return f_performNetCheck;
  }

  /**
   * Constructs a new SureLogic license with a random identity via
   * {@link UUID#randomUUID()}.
   * 
   * @param holder
   *          a non-<tt>null</tt> name of the license holder. Limited to 100
   *          characters.
   * @param email
   *          the email of the license holder, or {@code null} if not known.
   *          Limited to 254 characters.
   * @param company
   *          the company provided by the license holder, or {@code null} if
   *          none. Limited to 100 characters.
   * @param product
   *          the product to be licensed.
   * @param durationInDays
   *          the license duration in days from installation until expiration or
   *          renewal. This value must be greater than one.
   * @param installBeforeDate
   *          the installation deadline for this license, may be {@code null} to
   *          indicate no deadline.
   * @param type
   *          type of license.
   * @param maxActive
   *          the number of active installations allowed before an attempted
   *          installation fails. This value must be greater than zero.
   * @param performNetCheck
   *          {@code true} if net checks are required for the license,
   *          {@code false} otherwise.
   * 
   * @throws IllegalArgumentException
   *           if any of the parameters are {@code null} that should not be or
   *           if the license is {@link SLLicenseType#PERPETUAL} and no net
   *           check is required (which is not allowed).
   */
  public SLLicense(final @NonNull String holder, @Nullable final String email, @Nullable final String company,
      final @NonNull SLLicenseProduct product, final int durationInDays, final @Nullable @Unique Date installBeforeDate,
      final @NonNull SLLicenseType type, final int maxActive, final boolean performNetCheck) {
    this(UUID.randomUUID(), holder, email, company, product, durationInDays, installBeforeDate, type, maxActive, performNetCheck);
  }

  /**
   * Constructs a new SureLogic license.
   * 
   * @param uuid
   *          a non-<tt>null</tt> {@link UUID} for the license.
   * @param holder
   *          a non-<tt>null</tt> name of the license holder. Limited to 100
   *          characters.
   * @param email
   *          the email of the license holder, or {@code null} if not known.
   *          Limited to 254 characters.
   * @param company
   *          the company provided by the license holder, or {@code null} if
   *          none. Limited to 100 characters.
   * @param product
   *          the product to be licensed.
   * @param durationInDays
   *          the license duration in days from installation until expiration or
   *          renewal. This value must be greater than one.
   * @param installBeforeDate
   *          the installation deadline for this license, may be {@code null} to
   *          indicate no deadline.
   * @param type
   *          type of license.
   * @param maxActive
   *          the number of active installations allowed before an attempted
   *          installation fails. This value must be greater than zero.
   * @param performNetCheck
   *          {@code true} if net checks are required for the license,
   *          {@code false} otherwise.
   * 
   * @throws IllegalArgumentException
   *           if any of the parameters are {@code null} that should not be or
   *           too long. Also if the license is {@link SLLicenseType#PERPETUAL}
   *           and no net check is required (which is not allowed).
   */
  public SLLicense(@NonNull UUID uuid, @NonNull String holder, @Nullable String email, @Nullable String company,
      @NonNull SLLicenseProduct product, int durationInDays, @Nullable @Unique Date installBeforeDate, @NonNull SLLicenseType type,
      int maxActive, boolean performNetCheck) {
    if (uuid == null) {
      throw new IllegalArgumentException(I18N.err(44, "uuid"));
    }
    f_uuid = uuid;
    if (holder == null)
      throw new IllegalArgumentException(I18N.err(44, "holder"));
    else if (holder.length() > 100)
      throw new IllegalArgumentException(I18N.err(355, "holder", 100, holder.length()));
    f_holder = holder;
    if (email != null && email.length() > 254)
      throw new IllegalArgumentException(I18N.err(355, "email", 254, email.length()));
    f_email = email;
    if (company != null && company.length() > 100)
      throw new IllegalArgumentException(I18N.err(355, "company", 100, company.length()));
    f_company = company;
    if (product == null) {
      throw new IllegalArgumentException(I18N.err(44, "product"));
    }
    f_product = product;
    if (durationInDays <= 1) {
      throw new IllegalArgumentException(I18N.err(196, durationInDays));
    }
    f_durationInDays = durationInDays;
    f_installBeforeDate = installBeforeDate;
    if (type == null) {
      throw new IllegalArgumentException(I18N.err(44, "type"));
    }
    f_type = type;
    if (maxActive < 1) {
      throw new IllegalArgumentException(I18N.err(176, maxActive));
    }
    f_maxActive = maxActive;
    /*
     * Perpetual licenses must perform net checks.
     */
    if (type == SLLicenseType.PERPETUAL && !performNetCheck) {
      throw new IllegalArgumentException(I18N.err(177));
    }
    f_performNetCheck = performNetCheck;
  }

  @Override
  public String toString() {
    return SLLicensePersistence.toString(this);
  }
}
