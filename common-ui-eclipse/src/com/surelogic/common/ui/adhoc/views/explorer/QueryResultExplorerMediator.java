package com.surelogic.common.ui.adhoc.views.explorer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.Nullable;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.ui.EclipseColorUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class QueryResultExplorerMediator extends AdHocManagerAdapter implements ILifecycle {

  final AbstractQueryResultExplorerView f_view;
  final AdHocManager f_manager;
  final QueryResultNavigator f_navigator;
  final TreeViewer f_queryHistoryTree;
  final QueryResultExplorerContentProvider f_contentProvider = new QueryResultExplorerContentProvider();

  class ResultExplorerCellLabelProvide extends StyledCellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      final Object rawElement = cell.getElement();
      if (rawElement instanceof AdHocQueryResult) {
        final AdHocQueryResult result = (AdHocQueryResult) rawElement;
        final String label = f_view.getLabelFor(result);

        /*
         * Match Eclipse with uses subtle text color for " at time"
         */
        final int colonIndex = label.lastIndexOf(f_view.getWhereToStartSubtleTextColor());
        if (colonIndex != -1) {
          StyleRange[] ranges = { new StyleRange(colonIndex, label.length(), EclipseColorUtility.getSubtleTextColor(), null) };
          cell.setStyleRanges(ranges);
        }

        cell.setText(label);
        cell.setImage(SLImages.getImage(result.getImageSymbolicName()));
      } else
        super.update(cell);
    }
  };

  public QueryResultExplorerMediator(final AbstractQueryResultExplorerView view, final TreeViewer queryHistoryTree,
      final QueryResultNavigator navigator) {
    f_view = view;
    f_manager = view.getManager();
    f_queryHistoryTree = queryHistoryTree;
    f_navigator = navigator;
  }

  @Override
  public void init() {
    f_navigator.init();

    f_queryHistoryTree.setContentProvider(f_contentProvider);
    f_queryHistoryTree.setLabelProvider(new ResultExplorerCellLabelProvide());

    f_queryHistoryTree.getTree().addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final AdHocQueryResult selectedResult = getQueryHistoryTreeSelection();
        f_manager.setSelectedResult(selectedResult);
        if (selectedResult != null)
          f_manager.setQuerydoc(selectedResult.getQueryFullyBound().getQuery());
      }
    });

    f_manager.addObserver(this);
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
    f_navigator.dispose();
  }

  void setFocus() {
    f_queryHistoryTree.getTree().setFocus();
  }

  private final UIJob f_generalRefreshJob = new SLUIJob() {
    @Override
    public IStatus runInUIThread(final IProgressMonitor monitor) {
      updateQueryHistory();
      return Status.OK_STATUS;
    }
  };

  private void generalRefresh() {
    // schedule to run in the UI thread
    f_generalRefreshJob.schedule();
  }

  @Override
  public void notifyResultModelChange(final AdHocManager manager) {
    generalRefresh();
  }

  @Override
  public void notifySelectedResultChange(final AdHocQueryResult result) {
    generalRefresh();
  }

  void updateQueryHistory() {
    // Change input
    QueryResultExplorerContentProvider.Input newInput = new QueryResultExplorerContentProvider.Input(f_manager.getRootResultList());
    f_queryHistoryTree.setInput(newInput);

    // Set correct selection
    final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
    if (selectedResult != null)
      f_queryHistoryTree.setSelection(new StructuredSelection(selectedResult), true);
    else
      f_queryHistoryTree.setSelection(StructuredSelection.EMPTY);

    // Show everything
    f_queryHistoryTree.expandAll();
  }

  @Nullable
  AdHocQueryResult getQueryHistoryTreeSelection() {
    final IStructuredSelection s = (IStructuredSelection) f_queryHistoryTree.getSelection();
    if (!s.isEmpty()) {
      final Object o = s.getFirstElement();
      if (o instanceof AdHocQueryResult)
        return (AdHocQueryResult) o;
    }
    return null;
  }
}
