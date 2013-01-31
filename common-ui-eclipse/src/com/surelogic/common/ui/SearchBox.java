package com.surelogic.common.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.jobs.SLUIJob;

public final class SearchBox {

	private final Composite f_composite;

	public Composite getComposite() {
		return f_composite;
	}

	private final Text f_searchText;

	public Text getTextControl() {
		return f_searchText;
	}

	private final Label f_clearLabel;

	public Label getLabelControl() {
		return f_clearLabel;
	}

	private final String f_graySearchText;

	private final ISearchBoxObserver f_observer;

	private final Image f_clearImage = SLImages
			.getImage(CommonImages.IMG_GRAY_X_LIGHT);

	private final Image f_clearImageHover = SLImages
			.getImage(CommonImages.IMG_GRAY_X);

	private boolean f_searchTextInUse = false;

	public SearchBox(final Composite parent, final String graySearchText,
			final String clearToolTipText, final ISearchBoxObserver observer) {
		if (parent == null)
			throw new IllegalArgumentException("parent must be non-null");
		if (graySearchText == null)
			throw new IllegalArgumentException(
					"graySearchText must be non-null");
		if (clearToolTipText == null)
			throw new IllegalArgumentException(
					"clearToolTipText must be non-null");
		if (observer == null)
			throw new IllegalArgumentException("observer must be non-null");

		f_graySearchText = graySearchText;
		f_observer = observer;

		f_composite = new Composite(parent, SWT.NONE);
		f_composite.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
				false));
		final GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 2;
		compositeLayout.marginHeight = compositeLayout.marginWidth = 0;
		compositeLayout.horizontalSpacing = 3;
		f_composite.setLayout(compositeLayout);

		/*
		 * This label acts as a button via its low level SWT events. We do this
		 * to conserve UI space and make the button appear more flat.
		 */
		f_clearLabel = new Label(f_composite, SWT.NONE);
		f_clearLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
		f_clearLabel.setImage(f_clearImage);
		f_clearLabel.setToolTipText(clearToolTipText);
		f_clearLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (f_searchTextInUse) {
					clearText();
					notifyObserver();
				}
			}
		});
		f_clearLabel.addListener(SWT.MouseEnter, new Listener() {
			@Override
      public void handleEvent(Event event) {
				if (f_searchTextInUse) {
					f_clearLabel.setImage(f_clearImageHover);
				}
			}
		});
		f_clearLabel.addListener(SWT.MouseExit, new Listener() {
			@Override
      public void handleEvent(Event event) {
				f_clearLabel.setImage(f_clearImage);
			}
		});

		f_searchText = new Text(f_composite, SWT.NONE);
		f_searchText.setText(f_graySearchText);
		f_searchText.setForeground(f_searchText.getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		f_searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		f_searchText.addModifyListener(new ModifyListener() {
			@Override
      public void modifyText(ModifyEvent unused) {
				/*
				 * Check if the text is 'stable' in some amount of time ~500ms.
				 */
				final String old = f_searchText.getText();

				// lost the focus and we are now showing the gray search text
				if (!f_searchTextInUse && old.equals(f_graySearchText))
					return;

				final boolean inUse = !("".equals(old));
				final boolean wasInUse = f_searchTextInUse && !inUse;
				f_searchTextInUse = inUse;
				if (wasInUse) {
					notifyObserver();
				} else {
					if (f_searchTextInUse) {
						final UIJob job = new SLUIJob() {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								if (f_searchText.isDisposed())
									return Status.OK_STATUS;

								String current = f_searchText.getText();
								if (old.equals(current))
									notifyObserver();

								return Status.OK_STATUS;
							}
						};
						job.setSystem(true);
						job.schedule(500);
					}
				}
			}
		});
		f_searchText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (!f_searchTextInUse) {
					f_searchText.setText("");
					f_searchText.setForeground(null);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (!f_searchTextInUse) {
					f_searchText.setText(f_graySearchText);
					f_searchText.setForeground(f_searchText.getDisplay()
							.getSystemColor(SWT.COLOR_GRAY));
				}
			}
		});
	}

	/**
	 * Sets the search text of this search box.
	 * <p>
	 * No observer callbacks are made as a result of this action because the
	 * client invoked it and can take appropriate action.
	 * 
	 * @param text
	 *            the new search text. If the value is {@code null} or
	 *            {@code ""} then the search text is cleared via a call to
	 *            {@link #clearText()}.
	 */
	public void setText(final String text) {
		if (f_searchText.isDisposed())
			return;

		if (text == null || "".equals(text))
			clearText();
		else {
			f_searchText.setText(text);
			f_searchText.setForeground(null);
			f_searchTextInUse = true;
		}
	}

	/**
	 * Clears the search text of this search box.
	 * <p>
	 * No observer callbacks are made as a result of this action because the
	 * client invoked it and can take appropriate action.
	 */
	public void clearText() {
		if (f_searchText.isDisposed())
			return;

		if (f_searchText.isFocusControl()) {
			f_searchText.setText("");
		} else {
			f_searchText.setText(f_graySearchText);
			f_searchText.setForeground(f_searchText.getDisplay()
					.getSystemColor(SWT.COLOR_GRAY));
		}
		f_clearLabel.setImage(f_clearImage);
		f_searchTextInUse = false;
	}

	/**
	 * Must be invoked from the UI thread and no locks should ever be held.
	 */
	private void notifyObserver() {
		if (f_searchTextInUse)
			f_observer.searchTextChangedTo(f_searchText.getText());
		else
			f_observer.searchTextCleared();
	}
}
