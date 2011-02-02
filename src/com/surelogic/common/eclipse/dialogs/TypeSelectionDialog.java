package com.surelogic.common.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;

/**
 * Dialog box for selecting Java types from a given project.  Had to write this
 * because the built-in type selection dialog in Eclipse belongs to an 
 * internal package (stupid).  
 * 
 * <p>The Java project passed to the constructor bounds the type search to the
 * classes visible within that project.  
 * 
 * <p>The {@link #getResult()} method returns an array of {@link #IType} objects.
 * 
 * <p>The type search is performed in a separate thread.  An ongoing search
 * can be canceled by pressing the search cancel button, or by starting a new 
 * search.
 * 
 * <p>(Implementation note: don't forget to kill the current search before
 * exiting the dialog!)
 */
public class TypeSelectionDialog extends SelectionDialog {
  private static final Image IMG_ANNOTATION =
    SLImages.getImage(CommonImages.IMG_ANNOTATION);
  private static final Image IMG_CLASS =
    SLImages.getImage(CommonImages.IMG_CLASS);
  private static final Image IMG_INTERFACE =
    SLImages.getImage(CommonImages.IMG_INTERFACE);
  private static final Image IMG_ENUM =
    SLImages.getImage(CommonImages.IMG_ENUM);

  // Final field, initialized in constructor
  private final IJavaProject searchRoot;

  /* Final field, the contents are accessed in both the UI thread and the
   * search thread
   */
  private final List<IType> types =
    Collections.synchronizedList(new ArrayList<IType>());

  // Widgets, only used in the UI thread
  private TableViewer typesViewer;
  private ProgressBar progressBar;
  private Label statusLabel;
  private Text searchText = null;
  private Button cancelButton = null;
  
  /* Accessed in both the UI and search threads.  Content of the
   * progress monitor needs to be thread safe.
   */
  private volatile IProgressMonitor currentProgressMonitor = null;
  private volatile Thread searchThread = null;
  
  
  
  public TypeSelectionDialog(final Shell parentShell, final IJavaProject root) {
    super(parentShell);
    setTitle("Choose Java Type");
    setHelpAvailable(false);
    
    searchRoot = root;
  }

  @Override
  protected Control createDialogArea(final Composite container) {
    final Composite parent = (Composite) super.createDialogArea(container);
    ((GridLayout) parent.getLayout()).numColumns = 1;
    createSearchArea(parent);
    createStatusArea(parent);
    createListArea(parent);

    getShell().setMinimumSize(640, 480);
    return parent;
  }

  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    super.createButtonsForButtonBar(parent);
    getOkButton().setEnabled(false);
  }
  
  public void createSearchArea(final Composite parent) {
    final Composite searchArea = createNamedGroup(
        parent, "Enter Java type search pattern (wild card '*' allowed):", 1);
    searchArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final GridLayout searchAreaLayout = (GridLayout) searchArea.getLayout();

    // SEARCH is not available in Eclipse 3.2
    searchText = new Text(searchArea, SWT.SINGLE | SWT.SEARCH | SWT.CANCEL);
    searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    searchText.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (e.detail == SWT.CANCEL) {
          if (searchThread != null) {
            searchText.setEnabled(false);
            cancelSearch();
          }
        } else {
          startSearch(searchText.getText());
        }
      }
    });
    // Keep the CR from going to the default button
    searchText.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_RETURN) {
          e.doit = false;
          e.detail = SWT.TRAVERSE_NONE;
        }
      }
    });
    
    // If CANCEL flag on text field is not supported, add an explicit cancel button
    if ((searchText.getStyle() & SWT.CANCEL) == 0) {
      searchAreaLayout.numColumns = 2;
      cancelButton = createPushButton(searchArea, "Cancel");
      cancelButton.setEnabled(false);
      cancelButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent event) {
          cancelSearch();
        }
      });
    }
  }
  
  public void createStatusArea(final Composite parent) {
    final Composite statusArea = createNamedGroup(
        parent, "Search status", 1);
    statusArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    statusLabel = new Label(statusArea, SWT.NONE);
    statusLabel.setFont(statusArea.getFont());
    statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    statusLabel.setText("Enter search pattern");
    
    progressBar = new ProgressBar(statusArea, SWT.HORIZONTAL | SWT.SMOOTH);
    progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    progressBar.setMinimum(0);
    progressBar.setMaximum(1);
    progressBar.setSelection(0);
    progressBar.setEnabled(false);
  }
  
  private static Group createNamedGroup(Composite parent, String name,
      int columns) {
    final Group outer = new Group(parent, SWT.NONE);
    outer.setFont(parent.getFont());
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = columns;
    outer.setLayout(gridLayout);
    outer.setText(name);
    return outer;
  }

  private Button createPushButton(final Composite parent, final String label) {
    final Button button = new Button(parent, SWT.PUSH);
    button.setFont(parent.getFont());
    if (label != null) {
      button.setText(label);
    }
    GridData gd = new GridData();
    button.setLayoutData(gd); 
    return button;  
  }
  
  public void createListArea(final Composite parent) {
    typesViewer = new TableViewer(parent);
    typesViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

    typesViewer.setContentProvider(new IStructuredContentProvider() {
      @SuppressWarnings("unchecked")
      public Object[] getElements(Object inputElement) {
        return ((java.util.List<IType>) inputElement).toArray();
      }
      public void dispose() { /* do nothing */ }
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        /* do nothing */
      }
    });
    typesViewer.setLabelProvider(new ITableLabelProvider() {
      public Image getColumnImage(final Object element, final int columnIndex) {
        final IType type = (IType) element;
        try {
          if (type.isAnnotation()) {
            return IMG_ANNOTATION;
          } else if (type.isClass()) {
            return IMG_CLASS;
          } else if (type.isInterface()) {
            return IMG_INTERFACE;
          } else if (type.isEnum()) {
            return IMG_ENUM;
          } else {
            return null;
          }
        } catch (final JavaModelException e) {
          return null;
        }
      }

      public String getColumnText(final Object element, final int columnIndex) {
        return ((IType) element).getFullyQualifiedName('.');
      }

      public void dispose() {
        // Do nothing
      }
    
      public boolean isLabelProperty(final Object element,
          final String property) {
        return false;
      }
    
      public void addListener(final ILabelProviderListener listener) {
        // Do nothing
      }
    
      public void removeListener(final ILabelProviderListener listener) {
        // Do Nothing
      }
    });
    typesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(final SelectionChangedEvent event) {
        final ISelection selection = event.getSelection();
        if (selection.isEmpty()) {
          setResult(Collections.emptyList());
          getOkButton().setEnabled(false);
        } else {
          setResult(((StructuredSelection) selection).toList());
          getOkButton().setEnabled(true);
        }
      }      
    });    
    typesViewer.setComparator(new ViewerComparator());
    typesViewer.setInput(types);
  }

  private void startSearch(final String pattern) {
    /* Disable searchText to prevent another search without 
     * an explicit cancel.
     */
    searchText.setEditable(false);
    
    searchThread = new Thread() {
      @Override
      public void run() {
        final SearchPattern sp = SearchPattern.createPattern(
            pattern, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
            SearchPattern.R_PATTERN_MATCH);
        if (sp == null) return;
        
        final IJavaSearchScope scope = 
          SearchEngine.createJavaSearchScope(new IJavaElement[] { searchRoot });
        
        final SearchRequestor requestor = new SearchRequestor() {
          @Override
          public void beginReporting() {
            getShell().getDisplay().asyncExec(new Runnable() {
              public void run() {
                progressBar.setEnabled(true);
                if (cancelButton != null) cancelButton.setEnabled(true);
                types.clear();
                typesViewer.refresh();
              }
            });
          }
          
          @Override
          public void endReporting() {
            getShell().getDisplay().asyncExec(new Runnable() {
              public void run() {
                progressBar.setMaximum(1);
                progressBar.setSelection(0);
                progressBar.setEnabled(false);
                if (cancelButton != null) cancelButton.setEnabled(false);
                if (currentProgressMonitor.isCanceled()) {
                  statusLabel.setText("Search canceled");
                } else {
                  statusLabel.setText("Search finished");
                }
                currentProgressMonitor = null;
                searchText.setEditable(true);
                searchText.setEnabled(true); // may have been disabled via a cancel
              }
            });
          }
          
          @Override
          public void acceptSearchMatch(final SearchMatch match) throws CoreException {
            getShell().getDisplay().asyncExec(new Runnable() {
              public void run() {
                final IType element = (IType) match.getElement();
                types.add(element);
                typesViewer.add(element);
              }
            });
          }
          
        };
        
        currentProgressMonitor = new MyProgressMonitor();
        final SearchEngine engine = new SearchEngine();
        try {
          engine.search(sp,
              new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
              scope, requestor, currentProgressMonitor);
        } catch (final OperationCanceledException e) {
          // do nothing
        } catch (final CoreException e) {
          SLLogger.getLogger().log(Level.INFO, "Problem while searching for type", e);
        }
        searchThread = null;
        synchronized (TypeSelectionDialog.this) {
          TypeSelectionDialog.this.notify();
        }
      }
    };
    searchThread.start();
    statusLabel.setText("Searching...");
  }
  
  private void cancelSearch() {
    statusLabel.setText("Cancel pending...");
    if (cancelButton != null) cancelButton.setEnabled(false);
    currentProgressMonitor.setCanceled(true);
  }

  private void killCurrentSearch() {
    if (currentProgressMonitor != null) {
      cancelSearch();
    }
    while (searchThread != null) {
      synchronized (this) {
        try {
          wait();
        } catch (final InterruptedException e) {
          // ignore
        }
      }
    }
  }


  @Override
  protected void buttonPressed(final int buttonId) {
    /* Stop the search before we close the dialog */
    killCurrentSearch();    
    super.buttonPressed(buttonId);
  }

  
  
  private final class MyProgressMonitor implements IProgressMonitor {
    private boolean canceled = false;
    private int currentWork = 0;
    
    public MyProgressMonitor() { super(); }
    
    public void beginTask(final String name, final int totalWork) {
      getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          progressBar.setMaximum(totalWork);
        }
      });
    }
    
    public void internalWorked(final double work) {
      getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          progressBar.setSelection(currentWork += work);
        }
      });
    }

    public void worked(final int work) {
      internalWorked(work);
    }    

    public void done() { /* don't care */ }
    public synchronized boolean isCanceled() { return canceled; }
    public synchronized void setCanceled(final boolean value) { canceled = value; }
    public void setTaskName(final String name) { /* don't care */ }
    public void subTask(final String name) { /* don't care */ }
  }
}
