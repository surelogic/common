package com.surelogic.common.tool;

import com.surelogic.NonNull;

public interface SureLogicToolsFilter {

  /**
   * 
   * @param absoluteOrRelativePath
   * @param packageName
   * @return
   */
  boolean matches(@NonNull String absoluteOrRelativePath, @NonNull String packageName);
}
