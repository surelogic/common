package com.surelogic.common.ui.adhoc.views;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.IAdHocDataSource;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.adhoc.dialogs.ExportResultDataDialog;
import com.surelogic.common.ui.adhoc.dialogs.ShowSqlDialog;
import com.surelogic.common.ui.adhoc.dialogs.VariableValueDialog;
import com.surelogic.common.ui.jobs.SLUIJob;

/**
 * This class provides actions to navigate forward and backward among a tree of
 * ad hoc query results. A new instance is obtained via a call to
 * {@link #getInstance(IAdHocDataSource)} and then {@link #init()} should be
 * invoked on that instance (as well as {@link #dispose()} when the using view
 * is itself disposed).
 * <p>
 * The instance manages backward, forward, and other actions that can be
 * obtained by calling {@link QueryResultNavigator#getBackwardAction()},
 * {@link #getForwardAction()} and so forth. The instance, by listening to the
 * correct ad hoc query manager, enables and disables the actions.
 * <p>
 * The {@code addEnable} methods, e.g.,
 * {@link #addEnableWhenAResultIsSelected(IAction)}, allow the view to have the
 * instance manage if an action is enabled in a menu or a toolbar based upon
 * properties of the selected query. Register all the actions you want managed
 * before you call {@link #init()}.
 */
public final class QueryResultNavigator extends AdHocManagerAdapter implements
		ILifecycle {

	/**
	 * Provides a new instance. {@link #init()} should be invoked on the
	 * instance. {@link #dispose()} should be later invoked on the instance when
	 * the using view is itself disposed.
	 * 
	 * @param source
	 *            the data source for a query manager.
	 * @return a new instance.
	 */
	public static QueryResultNavigator getInstance(IAdHocDataSource source) {
		if (source == null)
			throw new IllegalArgumentException(I18N.err(44, "source"));
		return new QueryResultNavigator(source);
	}

	private QueryResultNavigator(IAdHocDataSource source) {
		f_source = source;
	}

	private final IAdHocDataSource f_source;

	private AdHocManager getAdHocManager() {
		return AdHocManager.getInstance(f_source);
	}

	private AdHocQueryResult getSelectedResult() {
		return getAdHocManager().getSelectedResult();
	}

	private AdHocQueryResult getBackwardTarget() {
		final AdHocQueryResult selected = getSelectedResult();
		if (selected != null) {
			final AdHocQueryResult target = selected.getParent();
			return target;
		}
		return null;
	}

	private final Action f_backward = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult target = getBackwardTarget();
			if (target != null) {
				target.getManager().setSelectedResult(target);
			}
		}
	};

	public Action getBackwardAction() {
		return f_backward;
	}

	private AdHocQueryResult getForwardTarget() {
		final AdHocQueryResult selected = getSelectedResult();
		if (selected != null) {
			if (selected.hasChildren()) {
				final AdHocQueryResult firstChild = selected.getChildrenList()
						.get(0);
				return firstChild;
			}
		}
		return null;
	}

	private final Action f_forward = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult target = getForwardTarget();
			if (target != null) {
				target.getManager().setSelectedResult(target);
			}
		}
	};

	public Action getForwardAction() {
		return f_forward;
	}

	@Override
	public void notifySelectedResultChange(AdHocQueryResult result) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				updateControlState();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private final Action f_disposeAction = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult selected = getSelectedResult();
			if (selected != null) {
				final AdHocManager manager = selected.getManager();
				selected.delete();
				manager.notifyResultModelChange();
			}
		}
	};

	public Action getDisposeAction() {
		return f_disposeAction;
	}

	private final Action f_disposeAllAction = new Action() {
		@Override
		public void run() {
			final AdHocManager manager = getAdHocManager();
			manager.deleteAllResults();
			manager.notifyResultModelChange();
		}
	};

	public Action getDisposeAllAction() {
		return f_disposeAllAction;
	}

	private final Action f_clearSelectionAction = new Action() {
		@Override
		public void run() {
			final AdHocManager manager = getAdHocManager();
			manager.setSelectedResult(null);
		}
	};

	public Action getClearSelectionAction() {
		return f_clearSelectionAction;
	}

	private final Action f_showDefinedVariablesAction = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult selected = getSelectedResult();
			if (selected != null) {
				final Map<String, String> variableValues = selected
						.getQueryFullyBound().getVariableValues();
				VariableValueDialog.openReadOnly(variableValues);
			}
		}
	};

	public Action getShowDefinedVariablesAction() {
		return f_showDefinedVariablesAction;
	}

	private final Action f_showSqlAction = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult selected = getSelectedResult();
			if (selected != null) {
				ShowSqlDialog.open(selected.getQueryFullyBound().getSql());
			}
		}
	};

	public Action getShowSqlAction() {
		return f_showSqlAction;
	}

	private final Action f_exportAction = new Action() {
		@Override
		public void run() {
			final AdHocQueryResult selected = getSelectedResult();
			if (selected instanceof AdHocQueryResultSqlData) {
				ExportResultDataDialog.open((AdHocQueryResultSqlData) selected);
			}
		}
	};

	public Action getExportAction() {
		return f_exportAction;
	}

	private final Set<IAction> f_enableWhenAResultExists = new CopyOnWriteArraySet<IAction>();

	public void addEnableWhenAResultExists(IAction action) {
		if (action != null) {
			f_enableWhenAResultExists.add(action);
		}
	}

	private final Set<IAction> f_enableWhenAResultIsSelected = new CopyOnWriteArraySet<IAction>();

	public void addEnableWhenAResultIsSelected(IAction action) {
		if (action != null) {
			f_enableWhenAResultIsSelected.add(action);
		}
	}

	private final Set<IAction> f_enableEnableWhenResultHasData = new CopyOnWriteArraySet<IAction>();

	public void addEnableWhenResultHasData(IAction action) {
		if (action != null) {
			f_enableEnableWhenResultHasData.add(action);
		}
	}

	private final Set<IAction> f_enableEnableWhenResultIsATree = new CopyOnWriteArraySet<IAction>();

	public void addEnableWhenResultIsATree(IAction action) {
		if (action != null) {
			f_enableEnableWhenResultIsATree.add(action);
		}
	}

	/**
	 * Must be run from the SWT thread.
	 */
	private void initActions() {
		f_backward.setText(I18N.msg("adhoc.query.navigator.backward"));
		f_backward.setToolTipText(I18N
				.msg("adhoc.query.navigator.backward.tooltip"));
		f_backward.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_LEFT));
		f_forward.setText(I18N.msg("adhoc.query.navigator.forward"));
		f_forward.setToolTipText(I18N
				.msg("adhoc.query.navigator.forward.tooltip"));
		f_forward.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_RIGHT));
		f_disposeAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_GRAY_X));
		f_disposeAction.setText(I18N.msg("adhoc.query.navigator.remove"));
		f_disposeAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.remove.tooltip"));
		addEnableWhenAResultIsSelected(f_disposeAction);
		f_disposeAllAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_GRAY_X_DOUBLE));
		f_disposeAllAction.setText(I18N.msg("adhoc.query.navigator.removeAll"));
		f_disposeAllAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.removeAll.tooltip"));
		addEnableWhenAResultExists(f_disposeAllAction);
		f_clearSelectionAction.setText(I18N
				.msg("adhoc.query.navigator.clearSelection"));
		f_clearSelectionAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.clearSelection.tooltip"));
		f_clearSelectionAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_QUERY_BACK));
		addEnableWhenAResultIsSelected(f_clearSelectionAction);
		f_showDefinedVariablesAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_INFO));
		f_showDefinedVariablesAction.setText(I18N
				.msg("adhoc.query.navigator.showVariables"));
		f_showDefinedVariablesAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.showVariables.tooltip"));
		addEnableWhenAResultIsSelected(f_showDefinedVariablesAction);
		f_showSqlAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_FILE));
		f_showSqlAction.setText(I18N.msg("adhoc.query.navigator.showSql"));
		f_showSqlAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.showSql.tooltip"));
		addEnableWhenAResultIsSelected(f_showSqlAction);
		f_exportAction.setImageDescriptor(SLImages
				.getImageDescriptor(CommonImages.IMG_EXPORT));
		f_exportAction.setText(I18N.msg("adhoc.query.navigator.exportData"));
		f_exportAction.setToolTipText(I18N
				.msg("adhoc.query.navigator.exportData.tooltip"));
		addEnableWhenResultHasData(f_exportAction);
	}

	/**
	 * Must be run from the SWT thread.
	 */
	private void updateControlState() {
		final boolean aResultExists = getAdHocManager().getResultCount() > 0;
		final AdHocQueryResult result = getSelectedResult();
		boolean hasAParent = false;
		boolean hasAChild = false;
		boolean hasData = false;
		boolean isTree = false;
		final boolean aResultIsSelected = result != null;
		if (aResultIsSelected) {
			hasAParent = getBackwardTarget() != null;
			hasAChild = getForwardTarget() != null;
			hasData = result instanceof AdHocQueryResultSqlData;
			if (hasData) {
				isTree = !((AdHocQueryResultSqlData) result).getModel()
						.isPureTable();
			}
		}
		f_backward.setEnabled(hasAParent);
		f_forward.setEnabled(hasAChild);
		for (IAction action : f_enableWhenAResultExists) {
			action.setEnabled(aResultExists);
		}
		for (IAction action : f_enableWhenAResultIsSelected) {
			action.setEnabled(aResultIsSelected);
		}
		for (IAction action : f_enableEnableWhenResultHasData) {
			action.setEnabled(hasData);
		}
		for (IAction action : f_enableEnableWhenResultIsATree) {
			action.setEnabled(isTree);
		}
	}

	/**
	 * Must be run from the SWT thread.
	 */
	public void init() {
		getAdHocManager().addObserver(this);
		initActions();
		updateControlState();
	}

	public void dispose() {
		getAdHocManager().removeObserver(this);
		f_enableWhenAResultExists.clear();
		f_enableWhenAResultIsSelected.clear();
		f_enableEnableWhenResultHasData.clear();
		f_enableEnableWhenResultIsATree.clear();
	}
}
