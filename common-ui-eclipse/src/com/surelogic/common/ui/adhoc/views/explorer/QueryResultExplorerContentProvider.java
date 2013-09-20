package com.surelogic.common.ui.adhoc.views.explorer;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.i18n.I18N;

public class QueryResultExplorerContentProvider implements ITreeContentProvider {

  static class Input {
    List<AdHocQueryResult> f_results;

    Input(List<AdHocQueryResult> results) {
      if (results == null)
        throw new IllegalArgumentException(I18N.err(44, "results"));
      f_results = results;
    }
  }

  private AdHocQueryResult[] f_root = null;

  @Override
  public void dispose() {
    // nothing to do
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput instanceof Input) {
      final Input in = (Input) newInput;
      f_root = in.f_results.toArray(new AdHocQueryResult[in.f_results.size()]);
    } else {
      f_root = null;
    }
  }

  @Override
  public Object[] getElements(Object inputElement) {
    final AdHocQueryResult[] root = f_root;
    return root != null ? root : AdHocQueryResult.EMPTY;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof AdHocQueryResult) {
      final List<AdHocQueryResult> asList = ((AdHocQueryResult) parentElement).getChildrenList();
      return asList.toArray(new AdHocQueryResult[asList.size()]);
    } else
      return AdHocQueryResult.EMPTY;
  }

  @Override
  public Object getParent(Object element) {
    if (element instanceof AdHocQueryResult)
      return ((AdHocQueryResult) element).getParent();
    else
      return null;
  }

  @Override
  public boolean hasChildren(Object element) {
    if (element instanceof AdHocQueryResult)
      return ((AdHocQueryResult) element).hasChildren();
    else
      return false;
  }
}
