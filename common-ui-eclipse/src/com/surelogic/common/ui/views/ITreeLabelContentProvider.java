package com.surelogic.common.ui.views;

import org.eclipse.jface.viewers.*;

public interface ITreeLabelContentProvider extends ITreeContentProvider, ILabelProvider {
  /**
   * By default do nothing.
   */
  default public void dispose() {
  }
}
