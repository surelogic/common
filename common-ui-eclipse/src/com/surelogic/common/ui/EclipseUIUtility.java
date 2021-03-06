package com.surelogic.common.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.Justification;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

@Utility
public final class EclipseUIUtility {

  /**
   * Gets the Eclipse {@link IPreferenceStore} for the SureLogic Eclipse-based
   * tools. This is usable in the Eclipse UI, most notably in preference
   * dialogs.
   * <p>
   * These are the same preferences returned by
   * {@link EclipseUtility#getPreferences()}, however, the
   * {@link IEclipsePreferences} returned by that method cannot be used in
   * preference dialogs. It should, however, be used in all other code to avoid
   * Eclipse UI dependencies.
   * <p>
   * These preferences are persisted within per-workspace.
   * 
   * @return the SureLogic Eclipse preferences usable in preference dialogs.
   * 
   * @see EclipseUtility#getPreferences()
   * @see EclipseUtility#PREFERENCES_NODE
   */
  @SuppressWarnings("deprecation")
  public static IPreferenceStore getPreferences() {
    return new ScopedPreferenceStore(new InstanceScope(), EclipseUtility.PREFERENCES_NODE);
  }

  /**
   * Determines if the passed perspective id is the currently opened perspective
   * in the workbench.
   * 
   * @param id
   *          the identifier of a perspective.
   * @return {@code true} if the passed perspective id is the currently opened
   *         perspective in the workbench, {@code false} otherwise.
   */
  public static boolean isPerspectiveOpen(final String id) {
    IWorkbenchWindow win = getIWorkbenchWindow();
    if (win != null) {
      IWorkbenchPage page = win.getActivePage();
      if (page != null) {
        IPerspectiveDescriptor desc = page.getPerspective();
        if (desc != null) {
          if (desc.getId().equals(id))
            return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the active workbench window, or {@code null} if none can be found.
   * 
   * @return the active workbench window, or {@code null} if none can be found.
   */
  public static IWorkbenchWindow getIWorkbenchWindow() {
    IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (win == null) {
      for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
        if (w != null) {
          return w;
        }
      }
    }
    return win;
  }

  /**
   * Gets the view if it is currently active in the workbench.
   * 
   * @param viewId
   *          the id of the view extension to use
   * @return the view or {@code null} if it can't be found.
   */
  public static IViewPart getView(final String viewId) {
    final IWorkbenchWindow window = getIWorkbenchWindow();
    if (window == null)
      return null;
    final IWorkbenchPage page = window.getActivePage();
    if (page == null)
      return null;
    IViewReference[] refs = page.getViewReferences();
    if (refs == null)
      return null;
    for (IViewReference vr : refs) {
      String id = vr.getId();
      if (id != null && id.equals(viewId)) {
        return vr.getView(false);
      }
    }
    return null;
  }

  /**
   * Shows the view identified by the given view id in this page and gives it
   * focus. If there is a view identified by the given view id (and with no
   * secondary id) already open in this page, it is given focus.
   * <P>
   * This method must be called from a UI thread or it will throw a
   * {@link NullPointerException}. *
   * 
   * @param viewId
   *          the id of the view extension to use
   * @return the shown view or {@code null}.
   */
  public static IViewPart showView(final String viewId) {
    final IWorkbenchWindow window = getIWorkbenchWindow();
    if (window == null)
      return null;
    final IWorkbenchPage page = window.getActivePage();
    if (page == null)
      return null;
    try {
      final IViewPart view = page.showView(viewId);
      return view;
    } catch (PartInitException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Unable to open the view identified by " + viewId + ".", e);
    }
    return null;
  }

  /**
   * Shows the view identified by the given view id and secondary id.
   * 
   * @param viewId
   *          the id of the view extension to use
   * @param secondaryId
   *          the secondary id to use, or {@code null} for no secondary id
   * @param mode
   *          the activation mode. Must be {@link IWorkbenchPage#VIEW_ACTIVATE},
   *          {@link IWorkbenchPage#VIEW_VISIBLE} or
   *          {@link IWorkbenchPage#VIEW_CREATE}
   * 
   * @return the shown view or {@code null}.
   */
  public static IViewPart showView(final String viewId, final String secondaryId, final int mode) {
    final IWorkbenchWindow window = getIWorkbenchWindow();
    if (window == null)
      return null;
    final IWorkbenchPage page = window.getActivePage();
    if (page == null)
      return null;
    try {
      final IViewPart view = page.showView(viewId, secondaryId, mode);
      return view;
    } catch (PartInitException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Unable to open the view identified by " + viewId + " " + secondaryId + ".", e);
    }
    return null;
  }

  /**
   * Shows the perspective identified by the given id.
   * 
   * @param perspectiveId
   *          the perspective to show.
   */
  public static void showPerspective(final String perspectiveId) {
    IPerspectiveDescriptor p = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(p);
    } catch (NullPointerException e) {
      // ignore
    }
  }

  /*
   * Adapted from the below article
   * http://blog.zvikico.com/2008/07/eclipse-gracefully-loading-a-plugin.html
   */

  private static Shell findStartedShell() {
    Shell result = null;
    final Display currentDisplay = getDisplay();
    if (currentDisplay != null) {
      result = currentDisplay.getActiveShell();
      if ((result != null) && (result.getMenuBar() == null)) {
        result = null;
      }
    }
    return result;
  }

  public static void startup(final IRunnableWithProgress startupOp) throws InvocationTargetException, InterruptedException {
    final Shell activeShell = findStartedShell();
    if (activeShell != null) {
      final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(activeShell);
      progressMonitorDialog.run(false, false, startupOp);
    } else {
      startupOp.run(new NullProgressMonitor());
    }
  }

  /**
   * Gets the shell being used by the active workbench window. This method
   * should always be invoked from within the SWT UI thread.
   * 
   * @return a shell or {@code null} if the shell being used by the active
   *         workbench window can't be determined.
   */
  @Nullable
  public static Shell getShell() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    return window == null ? null : window.getShell();
  }

  /**
   * Gets the default display, may be called from any thread.
   * 
   * @return the default display.
   */
  @NonNull
  public static Display getDisplay() {
    return Display.getDefault();
  }

  /**
   * Causes the run() method of the runnable to be invoked by the user-interface
   * thread at the next reasonable opportunity. The caller of this method
   * continues to run in parallel, and is not notified when the runnable has
   * completed. Specifying {@code null} as the runnable causes this method to
   * simply return.
   * 
   * @param job
   *          code to run on the user-interface thread or {@code null}
   * 
   * @see #nowOrAsyncExec(Runnable)
   */
  public static void asyncExec(@Nullable Runnable job) {
    if (job != null)
      getDisplay().asyncExec(job);
  }

  /**
   * Causes the run() method of the runnable to be invoked by the user-interface
   * thread just before the default display is disposed. Specifying {@code null}
   * as the runnable causes this method to simply return.
   * 
   * Parameters: runnable
   * 
   * @param job
   *          code to run at dispose time.
   */
  public static void disposeExec(@Nullable Runnable job) {
    if (job != null)
      getDisplay().disposeExec(job);
  }

  /**
   * Causes the run() method of the runnable to be invoked by the user-interface
   * thread now if this method is invoked in the context of the SWT UI thread or
   * at the next reasonable opportunity via {@link #asyncExec(Runnable)}.
   * Specifying {@code null} as the runnable causes this method to simply
   * return.
   * 
   * @param job
   *          code to run on the user-interface thread or {@code null}
   */
  public static void nowOrAsyncExec(@Nullable Runnable job) {
    if (job != null) {
      if (isUIThread())
        job.run();
      else
        asyncExec(job);
    }
  }

  /**
   * Gets if this call was invoked from the SWT UI thread.
   * 
   * @return {@code true} if this call was invoked from the SWT UI thread,
   *         {@code false} otherwise.
   */
  public static boolean isUIThread() {
    Object uiThread = getDisplay().getThread();
    return (Thread.currentThread() == uiThread);
  }

  /**
   * Adapts an IDE independent justification to Eclipse.
   * 
   * @param value
   *          the justification.
   * @return the Eclipse justification.
   */
  public static int adaptJustification(final Justification value) {
    switch (value) {
    case RIGHT:
      return SWT.RIGHT;
    case LEFT:
      return SWT.LEFT;
    case CENTER:
      return SWT.CENTER;
    default:
      throw new AssertionError(I18N.err(100, value.toString()));
    }
  }

  /**
   * Open the file with the given path with an Eclipse editor
   */
  public static IEditorPart openInEditor(String path) {
    IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
    if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
      final IWorkbenchWindow window = getIWorkbenchWindow();
      if (window != null) {
        final IWorkbenchPage page = window.getActivePage();
        try {
          IEditorPart p = IDE.openEditorOnFileStore(page, fileStore);
          return p;
        } catch (PartInitException e) {
          /* some code */
        }
      }
    }
    return null;
  }

  public static IEditorPart openInEditor(IEditorInput input, String editorId) {
    final IWorkbenchWindow window = getIWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      try {
        IEditorPart p = IDE.openEditor(page, input, editorId);
        return p;
      } catch (PartInitException e) {
        /* some code */
      }
    }
    return null;
  }

  /**
   * Builds the list of editors that apply to this build that need to be saved
   * 
   * @param projects
   *          the projects involved in this build, used to scope the searching
   *          process
   * @return the list of dirty editors for this launch to save, never null
   */
  public static IResource[] getScopedDirtyResources(IProject[] projects) {
    final Set<IResource> dirtyres = new HashSet<>();
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (int l = 0; l < windows.length; l++) {
      IWorkbenchPage[] pages = windows[l].getPages();
      for (int i = 0; i < pages.length; i++) {
        IEditorPart[] eparts = pages[i].getDirtyEditors();
        for (int j = 0; j < eparts.length; j++) {
          IResource resource = (IResource) eparts[j].getEditorInput().getAdapter(IResource.class);
          if (resource != null) {
            for (int k = 0; k < projects.length; k++) {
              if (projects[k].equals(resource.getProject())) {
                dirtyres.add(resource);
              }
            }
          }
        }
      }
    }
    return dirtyres.toArray(new IResource[dirtyres.size()]);
  }

  /**
   * Interface to fill the context menu when it is created. Implementations
   * should be passed to
   * {@link EclipseUIUtility#hookContextMenu(IViewPart, StructuredViewer, IContextMenuFiller)}
   */
  public static interface IContextMenuFiller {
    void fillContextMenu(IMenuManager m, IStructuredSelection s);
  }

  /**
   * Convenience method to hook a context menu to a structured editor in a view.
   * 
   * @param view
   *          the non-null view object (often <tt>this</tt>).
   * @param sv
   *          the non-null structured viewer
   * @param filler
   *          the non-null menu filler implementation (often <tt>this</tt>).
   */
  public static void hookContextMenu(final IViewPart view, final StructuredViewer sv, final IContextMenuFiller filler) {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager m) {
        final IStructuredSelection s = (IStructuredSelection) sv.getSelection();
        filler.fillContextMenu(m, s);
        // Other plug-ins can contribute there actions here
        m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
    Menu menu = menuMgr.createContextMenu(sv.getControl());
    sv.getControl().setMenu(menu);
    view.getSite().registerContextMenu(menuMgr, sv);
  }

  private EclipseUIUtility() {
    // utility
  }
}
