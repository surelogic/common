package com.surelogic.common.license;

import java.util.Date;
import java.util.UUID;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
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
  @NonNull
  private final Date f_installBeforeDate;

  /**
   * Gets installation deadline, or install before date, for this license.
   * Installations and activations after this date will fail.
   * 
   * @return the non-<tt>null</tt> installation deadline for this license.
   */
  @NonNull
  public Date getInstallBeforeDate() {
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
   * Constructs a new SureLogic license.
   * 
   * @param uuid
   *          a non-<tt>null</tt> {@link UUID} for the license.
   * @param holder
   *          a non-<tt>null</tt> name of the license holder.
   * @param product
   *          a non-<tt>null</tt> name of the product licensed.
   * @param durationInDays
   *          the license duration in days from installation until expiration or
   *          renewal. This value must be greater than one.
   * @param installBeforeDate
   *          the non-<tt>null</tt> installation deadline for this license.
   * @param type
   *          a non-<tt>null</tt> type for the license.
   * @param maxActive
   *          the number of active installations allowed before an attempted
   *          installation fails. This value must be greater than zero.
   * @param performNetCheck
   *          {@code true} if net checks are required for the license,
   *          {@code false} otherwise.
   * 
   * @throws IllegalArgumentException
   *           if any of the parameters are null that should not be.
   */
  public SLLicense(final @NonNull UUID uuid, final @NonNull String holder, final @NonNull SLLicenseProduct product,
      final int durationInDays, final @NonNull @Unique Date installBeforeDate, final @NonNull SLLicenseType type,
      final int maxActive, final boolean performNetCheck) {
    if (uuid == null) {
      throw new IllegalArgumentException(I18N.err(44, "uuid"));
    }
    f_uuid = uuid;
    if (holder == null) {
      throw new IllegalArgumentException(I18N.err(44, "holder"));
    }
    f_holder = holder;
    if (product == null) {
      throw new IllegalArgumentException(I18N.err(44, "product"));
    }
    f_product = product;
    if (durationInDays <= 1) {
      throw new IllegalArgumentException(I18N.err(196, durationInDays));
    }
    f_durationInDays = durationInDays;
    if (installBeforeDate == null) {
      throw new IllegalArgumentException(I18N.err(44, "installBeforeDate"));
    }
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
