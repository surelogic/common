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

  ALL_TOOLS("All Tools", false),

  FLASHLIGHT("Flashlight", true),

  FLASHLIGHT_ANDROID("Flashlight (Android)", true),

  JSURE("JSure", true),

  SIERRA("Sierra", true),

  EXEMPT("Exempt", true);

  /**
   * A text representation for this product.
   */
  private final String f_symbol;

  /**
   * {@code true} if this represents a licensed product that a license check can
   * be done on, {@code false} otherwise.
   */
  private final boolean f_isProduct;

  SLLicenseProduct(String symbol, boolean isProduct) {
    f_symbol = symbol;
    f_isProduct = isProduct;
  }

  /**
   * Checks if this represents a licensed product.
   * 
   * @return {@code true} if this represents a licensed product that a license
   *         check can be done on, {@code false} otherwise.
   */
  public boolean isProduct() {
    return f_isProduct;
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
