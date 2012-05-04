package com.surelogic.common.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
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
 * Can save the user interface state (what nodes are opened and what nodes are
 * closed) and selections of a {@link TreeViewer} so that it can be restored
 * after a update of its contents. This is done by saving textual labels into
 * paths if the node of the tree is open in the viewer when this object is
 * constructed. This approach avoids having to require that the viewer whose
 * state is saved is the viewer that gets restored.
 * <p>
 * Several static methods help register an auto-save onto a viewer when
 * something changes about the viewer and allow restore using that auto-save.
 * <p>
 * <i>Note: This object should be constructed and called in the SWT UI
 * thread.</i>
 */
public final class TreeViewerUIState {

	private static final String KEY = TreeViewerUIState.class.getName();

	/**
	 * Registers listeners that save the viewer's state when it changes and uses
	 * {@link Viewer#setData(String, Object)} to hold onto the created
	 * {@link TreeViewerUIState} object.
	 * <p>
	 * Use {@link TreeViewerUIState#updateTreeViewerState(TreeViewer)} to restore
	 * the viewer to this auto-saved state.
	 * 
	 * @param treeViewer
	 *            a viewer.
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
	 * Attempts to restore the viewer with the {@link TreeViewerUIState} object
	 * stored on it by {@link Viewer#setData(String, Object)}.
	 * 
	 * @param treeViewer
	 *            a viewer that
	 *            {@link #registerListenersToSaveTreeViewerStateOnChange(TreeViewer)}
	 *            was previously called on.
	 * @param matchOldAsSuffix
	 *            <tt>true</tt> if a matching label allows the old to be matched
	 *            as a suffix of the current label in the viewer, <tt>false</tt>
	 *            if the old and new labels must match exactly. See
	 *            {@link #restoreViewState(TreeViewer, boolean, boolean)} for
	 *            more information.
	 * @param expandSelections
	 *            <tt>true</tt> if selected nodes should be expanded even if
	 *            they were not in the saved state, <tt>false</tt> otherwise.
	 *            See {@link #restoreViewState(TreeViewer, boolean, boolean)}
	 *            for more information.
	 * @return <tt>true</tt> if the restore was successful, <tt>false</tt>
	 *         otherwise.
	 */
	public static boolean restoreSavedTreeViewerStateIfPossible(
			final TreeViewer treeViewer, boolean matchOldAsSuffix,
			boolean expandSelected) {
		if (treeViewer != null) {
			final Object o = treeViewer.getData(KEY);
			if (o instanceof TreeViewerUIState) {
				final TreeViewerUIState state = (TreeViewerUIState) o;
				state.restoreViewState(treeViewer, matchOldAsSuffix);
				return true;
			}
		}
		return false;
	}

	private final List<LinkedList<String>> f_stringPaths = new ArrayList<LinkedList<String>>();

	private final LinkedList<String> f_selectionPaths = new LinkedList<String>();

	/**
	 * Constructs a new instance saving the visible state (what nodes are opened
	 * and what nodes are closed) of the passed viewer as well as its
	 * selections. The viewer is <i>not</i> aliased into this object.
	 * 
	 * @param treeViewer
	 *            a viewer.
	 */
	public TreeViewerUIState(@Borrowed final TreeViewer treeViewer) {
		if (treeViewer == null)
			return;
		f_stringPaths.clear();
		final TreePath[] treePaths = treeViewer.getExpandedTreePaths();
		for (TreePath path : treePaths) {
			final LinkedList<String> stringPath = new LinkedList<String>();
			f_stringPaths.add(stringPath);
			for (int i = 0; i < path.getSegmentCount(); i++) {
				String message = path.getSegment(i).toString();
				stringPath.add(message);
			}
		}

		f_selectionPaths.clear();
		final ITreeSelection selection = (ITreeSelection) treeViewer
				.getSelection();
		if (selection != null) {
			final TreePath[] paths = selection.getPaths();
			if (paths != null && paths.length > 0) {
				final TreePath path = paths[0];
				for (int i = 0; i < path.getSegmentCount(); i++) {
					String message = path.getSegment(i).toString();
					f_selectionPaths.add(message);
				}
			}
		}
	}

	/**
	 * Restores the tree state and selections saved within this object to the
	 * passed viewer. Normally the viewer is the same object used to construct
	 * this, however, it is not required to be because the restore is done by
	 * matching text labels in the viewer.
	 * 
	 * @param treeViewer
	 *            a viewer.
	 * 
	 * @see #restoreViewState(TreeViewer, boolean)
	 */
	public final void restoreViewState(final TreeViewer treeViewer) {
		restoreViewState(treeViewer, false, false);
	}

	/**
	 * Restores the tree state and selections saved within this object to the
	 * passed viewer. Normally the viewer is the same object used to construct
	 * this, however, it is not required to be because the restore is done by
	 * matching text labels in the viewer.
	 * <p>
	 * The <tt>matchOldAsSuffix</tt> flag allows the old label to be a suffix of
	 * the current viewer label. This capability is useful if your label uses a
	 * convention such as <tt>&gt;</tt> to mark changes. For example, the viewer
	 * may label a modified package <tt>"&gt; my.package"</tt> but the old label
	 * might have just been <tt>"my.package"</tt>&mdash;of course we want these
	 * to match.
	 * 
	 * @param treeViewer
	 *            a viewer.
	 * @param matchOldAsSuffix
	 *            <tt>true</tt> if a matching label allows the old to be matched
	 *            as a suffix of the current label in the viewer, <tt>false</tt>
	 *            if the old and new labels must match exactly.
	 */
	public final void restoreViewState(final TreeViewer treeViewer,
			boolean matchOldAsSuffix) {
		restoreViewState(treeViewer, matchOldAsSuffix, false);
	}

	/**
	 * Restores the tree state and selections saved within this object to the
	 * passed viewer. Normally the viewer is the same object used to construct
	 * this, however, it is not required to be because the restore is done by
	 * matching text labels in the viewer.
	 * <p>
	 * The <tt>matchOldAsSuffix</tt> flag allows the old label to be a suffix of
	 * the current viewer label. This capability is useful if your label uses a
	 * convention such as <tt>&gt;</tt> to mark changes. For example, the viewer
	 * may label a modified package <tt>"&gt; my.package"</tt> but the old label
	 * might have just been <tt>"my.package"</tt>&mdash;of course we want these
	 * to match.
	 * <p>
	 * The <tt>expandSelections</tt> flag allows selected nodes to be expanded
	 * (opened) even if they were not when the viewer's state was saved. This
	 * can be useful for editors that add items, perhaps using a context menu,
	 * and want them to be visible when they are appear.
	 * 
	 * @param treeViewer
	 *            a viewer.
	 * @param matchOldAsSuffix
	 *            <tt>true</tt> if a matching label allows the old to be matched
	 *            as a suffix of the current label in the viewer, <tt>false</tt>
	 *            if the old and new labels must match exactly.
	 * @param expandSelections
	 *            <tt>true</tt> if selected nodes should be expanded even if
	 *            they were not in the saved state, <tt>false</tt> otherwise.
	 */
	public final void restoreViewState(final TreeViewer treeViewer,
			boolean matchOldAsSuffix, boolean expandSelections) {
		final IContentProvider cp = treeViewer.getContentProvider();
		if (cp instanceof ITreeContentProvider) {
			/*
			 * Restore the state of the tree (as best we can).
			 */
			final ITreeContentProvider tcp = (ITreeContentProvider) cp;
			for (LinkedList<String> path : f_stringPaths) {
				restoreSavedPath(treeViewer, tcp, path, null, matchOldAsSuffix);
			}

			/*
			 * Restore the selection (scrolls the view back to where the user
			 * was).
			 */
			if (!f_selectionPaths.isEmpty())
				restoreSavedSelections(treeViewer, tcp, f_selectionPaths, null,
						null, matchOldAsSuffix, expandSelections);
		} else {
			// System.out.println("Not a tree: "+cp);
		}
	}

	private void restoreSavedPath(final TreeViewer treeViewer,
			final ITreeContentProvider tcp, LinkedList<String> path,
			Object parent, boolean matchOldAsSuffix) {
		if (path.isEmpty())
			return;

		final Object[] elements;
		if (parent == null) {
			// at the root
			elements = tcp.getElements(null);
		} else {
			elements = tcp.getChildren(parent);
		}

		final String message = path.removeFirst();
		if (message == null)
			return;
		/*
		 * if (elements.length == 0) {
		 * System.out.println("No elts to restore to"); }
		 */
		for (Object element : elements) {
			String newMessage = element.toString();
			if (newMessage == null)
				newMessage = "";

			/*
			 * Match suffix of the new with the old or the whole string must
			 * match.
			 */
			if (matchOldAsSuffix ? newMessage.endsWith(message) : newMessage
					.equals(message)) {
				/*
				 * We have to be careful to only expand the last element in the
				 * path. This is because the getExpandedTreePaths states that
				 * it:
				 * 
				 * Returns a list of tree paths corresponding to expanded nodes
				 * in this viewer's tree, including currently hidden ones that
				 * are marked as expanded but are under a collapsed ancestor.
				 */
				if (path.isEmpty()) {
					// System.out.println("Expanded: "+message);
					treeViewer.setExpandedState(element, true);
				} else {
					restoreSavedPath(treeViewer, tcp, path, element,
							matchOldAsSuffix);
				}
			} else {
				// System.out.println("Couldn't find: "+message);
			}
		}
	}

	private void restoreSavedSelections(final TreeViewer treeViewer,
			final ITreeContentProvider tcp, LinkedList<String> path,
			Object parent, List<Object> treePath, boolean matchOldAsSuffix,
			boolean expandSelections) {
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
			return;

		boolean found = false;
		for (Object element : elements) {
			String newMessage = element.toString();
			if (newMessage == null)
				newMessage = "";

			/*
			 * Match suffix of the new with the old or the whole string must
			 * match.
			 */
			if (matchOldAsSuffix ? newMessage.endsWith(message) : newMessage
					.equals(message)) {
				found = true;
				treePath.add(element);
				if (path.isEmpty()) {
					/*
					 * Exact match of the old selection has been found.
					 */
					ISelection selection = new TreeSelection(new TreePath(
							treePath.toArray()));
					// System.out.println("Selected: "+message);
					treeViewer.setSelection(selection);
					if (expandSelections)
						treeViewer.setExpandedState(selection, true);
				} else {
					restoreSavedSelections(treeViewer, tcp, path, element,
							treePath, matchOldAsSuffix, expandSelections);
				}
			} else {
				// System.out.println("Couldn't find: "+message);
			}
		}
		/*
		 * In the case that part of the selection went away then select the
		 * remaining root of the same path.
		 */
		if (!found && !treePath.isEmpty()) {
			ISelection selection = new TreeSelection(new TreePath(
					treePath.toArray()));
			treeViewer.setSelection(selection);
			if (expandSelections)
				treeViewer.setExpandedState(selection, true);
		}
	}
}
