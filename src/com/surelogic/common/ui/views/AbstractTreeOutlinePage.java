/*
 * Created on Jan 25, 2005
 *
 */
package com.surelogic.common.ui.views;


import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Edwin
 */
public abstract class AbstractTreeOutlinePage extends AbstractOutlinePage {  
  @Override
  protected final Viewer makeViewer(Composite parent) {
    return new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    // return new TableTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
  }
  
  /**
   * Subclasses must extend this method configure the tree viewer 
   * with a proper content provider, label provider, and input element.
   */
  @Override
  public void createControl(Composite parent) {
    // Creates the standard tree viewer control
    super.createControl(parent);

    TreeViewer viewer = (TreeViewer) getViewer();
    initTreeViewer(viewer);
    /*
    viewer.setContentProvider(content);
    viewer.setLabelProvider(content);
    viewer.setInput(compUnit);
    viewer.expandToLevel(3);
    */
  }
  
  /**
   * Called from createControl()
   */
  protected abstract void initTreeViewer(TreeViewer tree);
}
