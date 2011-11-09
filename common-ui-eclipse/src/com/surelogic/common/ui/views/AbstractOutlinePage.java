package com.surelogic.common.ui.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * An abstract base class for content outline pages.
 * <p>
 * Clients who are defining an editor may elect to provide a corresponding
 * content outline page. This content outline page will be presented to the
 * user via the standard Content Outline View (the user decides whether their
 * workbench window contains this view) whenever that editor is active.
 * This class should be subclassed.
 * </p>
 * <p>
 * Internally, each content outline page consists of a standard tree viewer; 
 * selections made in the tree viewer are reported as selection change events 
 * by the page (which is a selection provider). The tree viewer is not created 
 * until <code>createPage</code> is called; consequently, subclasses must extend
 * <code>createControl</code> to configure the tree viewer with a proper content 
 * provider, label provider, and input element.
 * </p>
 * <p>
 * Note that those wanting to use a control other than internally created
 * <code>TreeViewer</code> will need to implement 
 * <code>IContentOutlinePage</code> directly rather than subclassing this class.
 * </p> 
 */
@SuppressWarnings("deprecation")
public abstract class AbstractOutlinePage extends Page implements
        IContentOutlinePage, ISelectionChangedListener {
    // FIX update for 3.2
    private ListenerList selectionChangedListeners = new ListenerList();
    private Viewer viewer;

    /**
     * Create a new content outline page.
     * Designed to only be created via a subclass
     */
    protected AbstractOutlinePage() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

    /**
     * The <code>ContentOutlinePage</code> implementation of this 
     * <code>IContentOutlinePage</code> method creates a tree viewer. Subclasses
     * must extend this method configure the tree viewer with a proper content 
     * provider, label provider, and input element.
     * @param parent
     */
    @Override
    public void createControl(Composite parent) {
        viewer = makeViewer(parent);
        viewer.addSelectionChangedListener(this);
    }
    
    protected abstract Viewer makeViewer(Composite parent);

    /**
     * Fires a selection changed event.
     *
     * @param selection the new selection
     */
    protected void fireSelectionChanged(ISelection selection) {
        // create an event
        final SelectionChangedEvent event = new SelectionChangedEvent(this,
                selection);

        // fire the event
        Object[] listeners = selectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    /* (non-Javadoc)
     * Method declared on IPage (and Page).
     */
    @Override
    public Control getControl() {
        if (viewer == null)
            return null;
        return viewer.getControl();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public ISelection getSelection() {
        if (viewer == null)
            return StructuredSelection.EMPTY;
        return viewer.getSelection();
    }

    /**
     * Returns this page's viewer.
     *
     * @return this page's viewer, or <code>null</code> if 
     *   <code>createControl</code> has not been called yet
     */
    protected Viewer getViewer() {
        return viewer;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
     */
    @Override
    public void init(IPageSite pageSite) {
        super.init(pageSite);
        pageSite.setSelectionProvider(this);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionChangeListener.
     * Gives notification that the tree selection has changed.
     */
    public void selectionChanged(SelectionChangedEvent event) {
        fireSelectionChanged(event.getSelection());
    }

    /**
     * Sets focus to a part in the page.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /* (non-Javadoc)
     * Method declared on ISelectionProvider.
     */
    public void setSelection(ISelection selection) {
        if (viewer != null)
            viewer.setSelection(selection);
    }
}
