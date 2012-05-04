package com.surelogic.common.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.surelogic.Borrowed;

/**
 * Can save the user interface state, nodes that are expanded and selected, of a
 * {@link TreeViewer} so that it can be restored after a update of its contents.
 * This is done by saving textual labels into paths if the node of the tree is
 * open in the viewer when this object is constructed. This heuristic approach
 * avoids having to require that the viewer whose state is saved is the viewer
 * that gets restored and supports persistence well.
 * <p>
 * Several static methods help register auto-save listeners onto a viewer (which
 * save the state when something changes about the viewer changes) and allow
 * restore using that auto-save.
 * <p>
 * <i>Note: This object should be constructed and called in the SWT UI
 * thread.</i>
 */
public final class TreeViewerUIState {

	private static final String KEY = TreeViewerUIState.class.getName();

	/**
	 * Registers listeners that auto-save the viewer's user interface state when
	 * it changes. (The implementation uses
	 * {@link Viewer#setData(String, Object)} to store the
	 * {@link TreeViewerUIState} object).
	 * <p>
	 * Use {@link TreeViewerUIState#updateTreeViewerState(TreeViewer)} to
	 * restore the viewer to this auto-saved state or
	 * {@link TreeViewerUIState#getSavedTreeViewerStateIfPossible(TreeViewer)}
	 * to get the auto-saved state.
	 * 
	 * @param treeViewer
	 *            a tree viewer.
	 */
	public static void registerListenersToSaveTreeViewerStateOnChange(
			final TreeViewer treeViewer) {
		if (treeViewer == null)
			return;

		treeViewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				updateTreeViewerState(treeViewer);
			}

			public void treeCollapsed(TreeExpansionEvent event) {
				updateTreeViewerState(treeViewer);
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateTreeViewerState(treeViewer);
			}
		});
	}

	private static void updateTreeViewerState(final TreeViewer treeViewer) {
		if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
			final TreeViewerUIState contentsState = new TreeViewerUIState(
					treeViewer);
			treeViewer.setData(KEY, contentsState);
		}
	}

	/**
	 * Gets the {@link TreeViewerUIState} stored on the tree viewer, if one
	 * exists. If a save has not been performed, <tt>null</tt> is returned.
	 * 
	 * @param treeViewer
	 *            a tree viewer that
	 *            {@link #registerListenersToSaveTreeViewerStateOnChange(TreeViewer)}
	 *            was previously called on.
	 * @return the saved state of <tt>treeViewer</tt>, or <tt>null</tt> if their
	 *         is none.
	 */
	public static TreeViewerUIState getSavedTreeViewerStateIfPossible(
			final TreeViewer treeViewer) {
		if (treeViewer != null) {
			final Object o = treeViewer.getData(KEY);
			if (o instanceof TreeViewerUIState) {
				final TreeViewerUIState state = (TreeViewerUIState) o;
				return state;
			}
		}
		return null;
	}

	/**
	 * Attempts to restore user interface state of a tree viewer with the
	 * {@link TreeViewerUIState} object stored on it. The return value indicates
	 * if the restore was successful.
	 * 
	 * @param treeViewer
	 *            a tree viewer that
	 *            {@link #registerListenersToSaveTreeViewerStateOnChange(TreeViewer)}
	 *            was previously called on.
	 * @return <tt>true</tt> if the restore was successful, <tt>false</tt>
	 *         otherwise.
	 */
	public static boolean restoreSavedTreeViewerStateIfPossible(
			final TreeViewer treeViewer) {
		return restoreSavedTreeViewerStateIfPossible(treeViewer, false);
	}

	/**
	 * Attempts to restore user interface state of a tree viewer with the
	 * {@link TreeViewerUIState} object stored on it. The return value indicates
	 * if the restore was successful.
	 * 
	 * @param treeViewer
	 *            a tree viewer that
	 *            {@link #registerListenersToSaveTreeViewerStateOnChange(TreeViewer)}
	 *            was previously called on.
	 * @param matchSuffix
	 *            <tt>true</tt> if a matching label allows the old to be matched
	 *            as a suffix of the current label or visa versa, <tt>false</tt>
	 *            if the old and new labels must match exactly. See
	 *            {@link #restoreViewState(TreeViewer, boolean, boolean)} for
	 *            more information.
	 * @return <tt>true</tt> if the restore was successful, <tt>false</tt>
	 *         otherwise.
	 */
	public static boolean restoreSavedTreeViewerStateIfPossible(
			final TreeViewer treeViewer, boolean matchSuffix) {
		if (treeViewer != null) {
			final TreeViewerUIState state = getSavedTreeViewerStateIfPossible(treeViewer);
			if (state != null) {
				state.restoreViewState(treeViewer, matchSuffix);
				return true;
			}
		}
		return false;
	}

	/*
	 * INSTANCE STATE AND METHODS
	 */

	/**
	 * Saved tree paths to expanded nodes.
	 */
	private final List<LinkedList<String>> f_expandedPaths = new ArrayList<LinkedList<String>>();

	/**
	 * Saved tree paths to selected nodes.
	 */
	private final List<LinkedList<String>> f_selectedPaths = new ArrayList<LinkedList<String>>();

	/**
	 * Constructs a new instance that saves the passed tree viewer's user
	 * interface state. This includes what nodes are expanded and what nodes are
	 * selected The viewer is <i>not</i> aliased into this object.
	 * 
	 * @param treeViewer
	 *            a tree viewer.
	 */
	public TreeViewerUIState(@Borrowed final TreeViewer treeViewer) {
		if (treeViewer == null)
			return;

		/*
		 * We need the label provider...return if we can't find one.
		 */
		IBaseLabelProvider baseLabelProvider = treeViewer.getLabelProvider();
		if (!(baseLabelProvider instanceof ILabelProvider))
			return;
		final ILabelProvider lp = (ILabelProvider) baseLabelProvider;

		savePathsToList(treeViewer.getExpandedTreePaths(), lp, f_expandedPaths);

		final ITreeSelection selection = (ITreeSelection) treeViewer
				.getSelection();
		savePathsToList(selection.getPaths(), lp, f_selectedPaths);
	}

	private void savePathsToList(final TreePath[] treePaths,
			final ILabelProvider lp, final List<LinkedList<String>> to) {
		for (TreePath path : treePaths) {
			final LinkedList<String> stringPath = new LinkedList<String>();
			to.add(stringPath);
			for (int i = 0; i < path.getSegmentCount(); i++) {
				final Object element = path.getSegment(i);
				final String message = lp.getText(element);
				stringPath.add(message == null ? "null" : message);
			}
		}
	}

	/**
	 * Restores the user interface state saved within this object to the passed
	 * tree viewer. The restoration is heuristic and is only a
	 * "best effort"&mdash;it may not be a perfect.
	 * <p>
	 * Normally the tree viewer is the same one used to construct this object,
	 * however, this is not required because the restore is done by matching
	 * text labels in the viewer.
	 * 
	 * @param treeViewer
	 *            a tree viewer.
	 * 
	 * @see #restoreViewState(TreeViewer, boolean, boolean)
	 */
	public final void restoreViewState(final TreeViewer treeViewer) {
		restoreViewState(treeViewer, false);
	}

	/**
	 * Restores the user interface state saved within this object to the passed
	 * tree viewer. The restoration is heuristic and is only a
	 * "best effort"&mdash;it may not be a perfect.
	 * <p>
	 * Normally the tree viewer is the same one used to construct this object,
	 * however, this is not required because the restore is done by matching
	 * text labels in the viewer.
	 * <p>
	 * The <tt>matchSuffix</tt> flag allows the old label to be a suffix of the
	 * current viewer label or visa versa. This capability is useful if your
	 * label uses a convention such as <tt>&gt;</tt> to mark changes. For
	 * example, the viewer may label a modified package
	 * <tt>"&gt; my.package"</tt> but the old label might have just been
	 * <tt>"my.package"</tt>&mdash;of course we want these to match on the
	 * restore.
	 * 
	 * @param treeViewer
	 *            a tree viewer.
	 * @param matchSuffix
	 *            <tt>true</tt> if a matching label allows the old to be matched
	 *            as a suffix of the current label or visa versa, <tt>false</tt>
	 *            if the old and new labels must match exactly.
	 */
	public final void restoreViewState(final TreeViewer treeViewer,
			boolean matchSuffix) {
		/*
		 * We need the content provider...return if we can't find one.
		 */
		final IContentProvider cp = treeViewer.getContentProvider();
		if (!(cp instanceof ITreeContentProvider))
			return;
		final ITreeContentProvider tcp = (ITreeContentProvider) cp;

		/*
		 * We need the label provider...return if we can't find one.
		 */
		IBaseLabelProvider baseLabelProvider = treeViewer.getLabelProvider();
		if (!(baseLabelProvider instanceof ILabelProvider))
			return;
		final ILabelProvider lp = (ILabelProvider) baseLabelProvider;

		/*
		 * Restore the expanded nodes in the tree (as best we can).
		 */
		for (LinkedList<String> path : f_expandedPaths) {
			restorePathsHelper(new restoreExpandedNodes(), treeViewer, tcp, lp,
					path, null, null, matchSuffix);
		}

		/*
		 * Restore the selections in the tree.
		 */
		for (LinkedList<String> path : f_selectedPaths) {
			restorePathsHelper(new restoreSelectedNodes(), treeViewer, tcp, lp,
					path, null, null, matchSuffix);
		}
	}

	private interface MatchCallback {
		void onMatch(TreeViewer treeViewer, Object element,
				List<Object> treePath);
	}

	private static class restoreExpandedNodes implements MatchCallback {
		public void onMatch(TreeViewer treeViewer, Object element,
				List<Object> treePath) {
			treeViewer.setExpandedState(element, true);
		}
	}

	private static class restoreSelectedNodes implements MatchCallback {
		public void onMatch(TreeViewer treeViewer, Object element,
				List<Object> treePath) {
			final ISelection selection = new TreeSelection(new TreePath(
					treePath.toArray()));
			treeViewer.setSelection(selection);
		}
	}

	private boolean match(String oldMessage, String newMessage,
			boolean matchSuffix) {
		if (oldMessage == null || newMessage == null)
			return false;

		final boolean match;
		if (matchSuffix) {
			/*
			 * Match suffix is in either direction: old is a suffix of new or
			 * new is a suffix of old.
			 */
			match = newMessage.endsWith(oldMessage)
					|| oldMessage.endsWith(newMessage);
		} else {
			match = newMessage.equals(oldMessage);
		}
		return match;
	}

	private void restorePathsHelper(final MatchCallback callback,
			final TreeViewer treeViewer, final ITreeContentProvider tcp,
			final ILabelProvider lp, LinkedList<String> path, Object parent,
			List<Object> treePath, boolean matchSuffix) {
		if (path.isEmpty())
			return;

		final Object[] elements;
		if (parent == null) {
			// at the root
			elements = tcp.getElements(null);
			treePath = new ArrayList<Object>();
		} else {
			elements = tcp.getChildren(parent);
		}

		final String message = path.removeFirst();
		if (message == null)
			return; // should not happen, but bail if it does

		for (Object element : elements) {
			String newMessage = lp.getText(element);
			if (newMessage == null)
				newMessage = "";

			if (match(message, newMessage, matchSuffix)) {
				treePath.add(element);
				if (path.isEmpty()) {
					callback.onMatch(treeViewer, element, treePath);
				} else {
					restorePathsHelper(callback, treeViewer, tcp, lp, path,
							element, treePath, matchSuffix);
				}
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(getClass().getName()).append('@')
				.append(Integer.toHexString(hashCode())).append("[\n");
		b.append("  expanded paths:\n");
		for (List<String> path : f_expandedPaths) {
			b.append("    ").append(path.toString()).append("\n");
		}
		b.append("  selected path:\n");
		for (List<String> path : f_selectedPaths) {
			b.append("    ").append(path.toString()).append("\n");
		}
		b.append("]");
		return b.toString();
	}
}
