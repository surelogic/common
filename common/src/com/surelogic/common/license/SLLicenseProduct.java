package com.surelogic.common.license;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.Immutable;
import com.surelogic.Vouch;
import com.surelogic.RegionEffects;

/**
 * The products licensed by SureLogic.
 */
@Immutable
public enum SLLicenseProduct {

  ALL_TOOLS("All Tools"),

  FLASHLIGHT("Flashlight"),

  FLASHLIGHT_ANDROID("Flashlight (Android)"),

  JSURE("JSure"),

  SIERRA("Sierra"),

  EXEMPT("Exempt");

  private final String f_symbol;

  SLLicenseProduct(String symbol) {
    f_symbol = symbol;
  }

  /**
   * Checks if this product requires a license for use.
   * 
   * @return {@code true} if a license is required to use this product,
   *         {@code false} otherwise.
   */
  public boolean needsLicense() {
    if (this == EXEMPT)
      return false;
    if (this == ALL_TOOLS)
      return false;
    return true;
  }

  /**
   * Checks if a license for the receiver product includes, or implies, a
   * license for the passed product.
   * 
   * @param product
   *          a product.
   * @return {@code true} if a license for the this product includes a license
   *         for <tt>product</tt>, {@code false} otherwise.
   */
  public boolean includes(final SLLicenseProduct product) {
    if (product == null)
      return false;
    if (this == product)
      return true;
    if (this == ALL_TOOLS)
      return true;
    if (this == FLASHLIGHT_ANDROID && product == FLASHLIGHT)
      return true;
    return false;
  }

  @RegionEffects("reads this:Instance")
  @Override
  public String toString() {
    return f_symbol;
  }

  /*
   * See page 154 of Bloch's <i>Effective Java</i> (second edition) for a
   * further description of supporting a fromString operation.
   */
  @Vouch("Immutable")
  private static final Map<String, SLLicenseProduct> stringToEnum = new HashMap<>();
  static {
    for (SLLicenseProduct type : values()) {
      stringToEnum.put(type.toString(), type);
    }
  }

  /**
   * Returns the product from its name.
   * 
   * @param value
   *          a product name, such as <tt>"All Tools"</tt>.
   * @return the product, or {@code null} if the passed value is not recognized.
   */
  public static SLLicenseProduct fromString(String value) {
    return stringToEnum.get(value);
  }
}
