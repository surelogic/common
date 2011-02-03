package com.surelogic.common.eclipse.adhoc.views.explorer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.adhoc.views.QueryResultNavigator;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.tooltip.ToolTip;
import com.surelogic.common.ui.SLImages;

public final class QueryResultExplorerMediator extends AdHocManagerAdapter
		implements ILifecycle {

	private final AdHocManager f_manager;
	private final QueryResultNavigator f_navigator;
	private final Tree f_queryHistoryTree;

	public QueryResultExplorerMediator(
			final AbstractQueryResultExplorerView view,
			final Tree queryHistoryTree, final QueryResultNavigator navigator) {
		f_manager = view.getManager();
		f_queryHistoryTree = queryHistoryTree;
		f_navigator = navigator;
	}

	public void init() {
		f_navigator.init();

		f_queryHistoryTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				final AdHocQueryResult selectedResult = getQueryHistoryTreeSelection();
				f_manager.setSelectedResult(selectedResult);
			}
		});

		f_manager.addObserver(this);
	}

	public void dispose() {
		f_manager.removeObserver(this);
		f_navigator.dispose();
	}

	void setFocus() {
		f_queryHistoryTree.setFocus();
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

	private void updateQueryHistory() {
		f_queryHistoryTree.setRedraw(false);
		f_queryHistoryTree.removeAll();

		for (final AdHocQueryResult result : f_manager.getResultList()) {
			if (result.getParent() == null) {
				addResultToTree(result, null);
			}
		}

		for (final TreeColumn c : f_queryHistoryTree.getColumns()) {
			c.pack();
		}
		setQueryHistoryTreeSelection(null);
		f_queryHistoryTree.setRedraw(true);
	}

	private void addResultToTree(final AdHocQueryResult result,
			final TreeItem parent) {
		final TreeItem item;
		if (parent == null) {
			item = new TreeItem(f_queryHistoryTree, SWT.NONE);
		} else {
			item = new TreeItem(parent, SWT.NONE);
		}
		f_queryHistoryTree.showItem(item);
		item.setText(result.toString());
		item.setImage(SLImages.getImage(result.getImageSymbolicName()));
		item.setData(result);
		item.setData(ToolTip.TIP_TEXT, result.getQueryFullyBound().getQuery()
				.getShortMessage());
		for (final AdHocQueryResult child : result.getChildrenList()) {
			addResultToTree(child, item);
		}
	}

	private void setQueryHistoryTreeSelection(final TreeItem parent) {
		final AdHocQueryResult selectedResult = f_manager.getSelectedResult();
		final TreeItem[] children;
		if (parent == null) {
			f_queryHistoryTree.deselectAll();
			if (selectedResult == null) {
				return; // bail nothing to select
			}
			children = f_queryHistoryTree.getItems();
		} else {
			final AdHocQueryResult itemResult = (AdHocQueryResult) parent
					.getData();
			if (itemResult == selectedResult) {
				f_queryHistoryTree.setSelection(parent);
				return; // bail only one can be selected
			}
			children = parent.getItems();
		}
		for (final TreeItem item : children) {
			setQueryHistoryTreeSelection(item);
		}
	}

	private AdHocQueryResult getQueryHistoryTreeSelection() {
		final TreeItem[] selectedItems = f_queryHistoryTree.getSelection();
		if (selectedItems.length == 1) {
			return (AdHocQueryResult) selectedItems[0].getData();
		} else {
			return null;
		}
	}
}
