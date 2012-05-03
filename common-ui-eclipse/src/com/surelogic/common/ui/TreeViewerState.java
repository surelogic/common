package com.surelogic.common.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.surelogic.Borrowed;

/**
 * Can save the user interface state (what nodes are opened and what nodes are
 * closed) and selections of a {@link TreeViewer} so that it can be restored
 * after a major update of its contents. This is done by saving textual labels
 * into paths if the node of the tree is open in the viewer when this object is
 * constructed. This approach avoids having to require that the viewer whose
 * state is saved is the viewer that gets restored.
 * <p>
 * <i>Note: This object should be constructed and called in the SWT UI
 * thread.</i>
 */
public final class TreeViewerState {

	private final List<LinkedList<String>> f_stringPaths = new ArrayList<LinkedList<String>>();

	private final LinkedList<String> f_selectionPath = new LinkedList<String>();

	/**
	 * Constructs a new instance saving the visible state (what nodes are opened
	 * and what nodes are closed) of the passed viewer as well as its
	 * selections. The viewer is <i>not</i> aliased into this object.
	 * 
	 * @param treeViewer
	 *            a viewer.
	 */
	public TreeViewerState(@Borrowed final TreeViewer treeViewer) {
		if (treeViewer != null) {
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

			f_selectionPath.clear();
			final ITreeSelection selection = (ITreeSelection) treeViewer
					.getSelection();
			if (selection != null) {
				final TreePath[] paths = selection.getPaths();
				if (paths != null && paths.length > 0) {
					final TreePath path = paths[0];
					for (int i = 0; i < path.getSegmentCount(); i++) {
						String message = path.getSegment(i).toString();
						f_selectionPath.add(message);
					}
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
		restoreViewState(treeViewer, false);
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
			if (!f_selectionPath.isEmpty())
				restoreSavedSelection(treeViewer, tcp, f_selectionPath, null,
						null, matchOldAsSuffix);
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

	private void restoreSavedSelection(final TreeViewer treeViewer,
			final ITreeContentProvider tcp, LinkedList<String> path,
			Object parent, List<Object> treePath, boolean matchOldAsSuffix) {
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
				} else {
					restoreSavedSelection(treeViewer, tcp, path, element,
							treePath, matchOldAsSuffix);
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
		}
	}
}
