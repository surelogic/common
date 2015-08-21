package com.surelogic.common.license;

import java.util.Date;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Represents a net check for a particular license. The clients pair these
 * instances with {@link SLLicense} instances if the license required a net
 * check to be installed. The digitally signed data that is used to construct
 * instances is constructed by the <i>surelogic.com</i> server as an affirmative
 * answer to installing or renewing a particular license.
 * <p>
 * The MAC addresses should be obtained using
 * {@link SLUtility#getMacAddressesOfThisMachine()}. The MAC addresses are used
 * to check that the registered license has not been moved from one computer to
 * another.
 */
public final class SLLicenseNetCheck {

  /**
   * The identify of the license license this net check refers to. May not be
   * <tt>null</tt>.
   */
  @NonNull
  private final UUID f_uuid;

  /**
   * Gets the {@link UUID} that identifies the license this net check refers to.
   * 
   * @return the non-<tt>null</tt> {@link UUID} the license this net check
   *         refers to.
   */
  @NonNull
  public UUID getUuid() {
    return f_uuid;
  }

  /**
   * The license expiration date or renewal deadline.
   */
  @NonNull
  private final Date f_date;

  /**
   * Gets the license expiration date or renewal deadline.
   * 
   * @return the non-<tt>null</tt> license expiration date or renewal deadline.
   */
  @NonNull
  public Date getDate() {
    return f_date;
  }

  /**
   * MAC addresses of the machine, such as <tt>60-a4-4c-61-20-40</tt>.
   * 
   * @see SLUtility#getMacAddressesOfThisMachine()
   */
  @NonNull
  private final ImmutableSet<String> f_macAddresses;;

  /**
   * Gets the (possibly empty) set of MAC addresses of the machine that asked
   * for this license net check. These can be used to check (roughly) that the
   * registered license has not been transferred to another computer.
   * 
   * @return a (possibly empty) set of MAC addresses encoded as strings, such as
   *         <tt>60-a4-4c-61-20-40</tt>.
   */
  @NonNull
  public ImmutableSet<String> getMacAddresses() {
    return f_macAddresses;
  }

  /**
   * Checks if the set of MAC addresses from the machine that registered this
   * license contains at least one of the passed MAC addresses.
   * <p>
   * A special case is if the set of MAC addresses from the machine that
   * registered this license is <i>empty</i> then this method always returns
   * {@code true}.
   * 
   * @param macAddresses
   *          the set of MAC addresses of the machine of interest. Empty or
   *          {@code null} indicates no MAC addresses.
   * @return {@code true} if the this net check was done from a machine with at
   *         least one of the passed MAC addresses (or the set of MAC addresses
   *         from the machine that registered this license is <i>empty</i>),
   *         {@code false} otherwise.
   */
  public boolean containsAtLeastOneMacAddress(@Nullable Iterable<String> macAddresses) {
    /*
     * Handle the special case of no network card on the registered machine.
     */
    if (f_macAddresses.isEmpty())
      return true;

    if (macAddresses != null)
      for (String ma : macAddresses)
        if (f_macAddresses.contains(ma))
          return true;
    return false;
  }

  /**
   * Constructs a new SureLogic license.
   * 
   * @param uuid
   *          a non-<tt>null</tt> {@link UUID} for the license this net check
   *          refers to.
   * @param date
   *          a non-<tt>null</tt> license expiration date or renewal deadline.
   */
  public SLLicenseNetCheck(final @NonNull UUID uuid, final @NonNull Date date, final @Nullable Iterable<String> macAddresses) {
    if (uuid == null)
      throw new IllegalArgumentException(I18N.err(44, "uuid"));
    f_uuid = uuid;
    if (date == null)
      throw new IllegalArgumentException(I18N.err(44, "date"));
    f_date = date;
    if (macAddresses == null)
      f_macAddresses = ImmutableSet.of();
    else
      f_macAddresses = ImmutableSet.copyOf(macAddresses);
  }

  @Override
  public String toString() {
    return SLLicensePersistence.toString(this);
  }
}
