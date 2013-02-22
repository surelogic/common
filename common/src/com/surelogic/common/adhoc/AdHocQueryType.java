package com.surelogic.common.adhoc;

import com.surelogic.common.CommonImages;

public enum AdHocQueryType {

  INFORMATION(CommonImages.IMG_INFO),

  ERROR(CommonImages.IMG_ERROR);

  private final String f_imageName;

  private AdHocQueryType(String imageName) {
    f_imageName = imageName;
  }

  public String getImageName() {
    return f_imageName;
  }

  public static String[] stringValues() {
    AdHocQueryType[] values = values();
    String[] result = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = values[i].toString();
    }
    return result;
  }
}
