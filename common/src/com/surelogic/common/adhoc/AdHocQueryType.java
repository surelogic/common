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
}
