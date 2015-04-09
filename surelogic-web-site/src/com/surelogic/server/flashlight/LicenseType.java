package com.surelogic.server.flashlight;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.common.license.SLLicenseProduct;

/**
 * An enumeration of all of the licenses, that SureLogic currently supports, and
 * their item codes.
 * 
 * @author nathan
 * 
 */
public enum LicenseType {

    /**
     * 1 year license, 1 person
     */
    FL_1_YEAR("FL001", "1 License", "300.00", 365 * 24 * 60 * 60 * 1000L, 1),
    /**
     * 3 1 year licenses
     */
    FL_1_YEAR_3PACK("FL001", "3 Licenses", "700.00",
            365 * 24 * 60 * 60 * 1000L, 3);

    private final String itemCode;
    private final String itemOption;
    private final String price;
    private final long duration;
    private final int count;

    private LicenseType(final String itemCode, final String option,
            final String price, final long duration, final int count) {
        this.itemCode = itemCode;
        this.itemOption = option;
        this.price = price;
        this.duration = duration;
        this.count = count;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemOption() {
        return itemOption;
    }

    public int getLicenseCount() {
        return count;
    }

    public long getDuration() {
        return duration;
    }

    public int getDurationInDays() {
        return (int) (duration / 1000 / 60 / 60 / 24);
    }

    public SLLicenseProduct getLicenseProduct() {
        return SLLicenseProduct.FLASHLIGHT;
    }

    /**
     * Return the license type associated with this item code, if and only if
     * the price is also correct
     * 
     * @param code
     * @param amount
     * @param currency
     * @return <code>null</code> if there is no match
     */
    public static LicenseType checkItemCode(final String code,
            final String option, final String amount, final String currency) {
        Map<String, LicenseType> map = codeMap.get(code);
        if (map != null) {
            final LicenseType type = map.get(option);
            if (type != null) {
                if ("USD".equals(currency)) {
                    if (type.price.equals(amount)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    private static final Map<String, Map<String, LicenseType>> codeMap = new HashMap<String, Map<String, LicenseType>>();
    static {
        for (final LicenseType t : values()) {
            Map<String, LicenseType> optionMap = codeMap.get(t.itemCode);
            if (optionMap == null) {
                optionMap = new HashMap<String, LicenseType>();
                codeMap.put(t.itemCode, optionMap);
            }
            optionMap.put(t.itemOption, t);
        }
    }

}
