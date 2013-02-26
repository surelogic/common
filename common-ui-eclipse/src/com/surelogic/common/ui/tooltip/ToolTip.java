package com.surelogic.common.ui.tooltip;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.HTMLPrinter;

/**
 * Handles pop-up help like the Javadoc pop-up help in the Eclipse Java editor.
 * To use construct a single instance per view and then register, via a call to
 * {@link #register(Control)} , each control that places {@link #TIP_TEXT} as
 * data (for example, on rows in a table or a tree).
 * <p>
 * See the {@link #mouseHover(MouseEvent)} method for those controls supported
 * by this class.
 */
public final class ToolTip implements KeyListener, MouseListener, MouseTrackListener, Listener {

  public static final String TIP_TEXT = "TIP_TEXT";

  @Nullable
  private static final String STYLE_SHEET = getStyleSheet();

  /**
   * Returns the Javadoc hover style sheet with the current Javadoc font from
   * the preferences.
   * 
   * @return the updated style sheet, may be {@code null}.
   */
  @Nullable
  private static String getStyleSheet() {
    String css = loadStyleSheet();
    if (css != null) {
      final FontData fontData = JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
      css = HTMLPrinter.convertTopLevelFont(css, fontData);
    }
    return css;
  }

  /**
   * Loads and returns the Javadoc hover style sheet.
   * 
   * @return the style sheet, or <code>null</code> if unable to load
   */
  private static String loadStyleSheet() {
    final URL styleSheetURL = Thread.currentThread().getContextClassLoader()
        .getResource("/com/surelogic/common/ui/tooltip/ToolTipHoverStyleSheet.css");
    if (styleSheetURL != null) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
        final StringBuffer buffer = new StringBuffer(1500);
        String line = reader.readLine();
        while (line != null) {
          buffer.append(line);
          buffer.append('\n');
          line = reader.readLine();
        }
        return buffer.toString();
      } catch (final IOException ex) {
        throw new IllegalStateException(ex);
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (final IOException e) {
        }
      }
    }
    return null;
  }

  @NonNull
  private static final TemporaryFileImageCache CACHE = new TemporaryFileImageCache();

  /**
   * Creates temporary files so that third party sources (such as browsers) can
   * view the files located in {@link CommonImages}.
   */
  private static class TemporaryFileImageCache implements ToolTipImageLoader {

    private final Map<String, File> fileMap = new HashMap<String, File>();

    @Override
    public File getImageFile(String gifName) {
      synchronized (fileMap) {
        File f = fileMap.get(gifName);
        if (f == null) {
          URL gifURL = CommonImages.getImageURL(gifName);
          if (gifURL != null) {
            try {
              f = File.createTempFile("IMAGE", "CACHE");
              f.deleteOnExit();
            } catch (IOException e) {
              throw new IllegalStateException(e);
            }
            FileUtility.copy(gifURL, f);
            return f.getAbsoluteFile();
          } else {
            return null;
          }
        } else {
          return f.getAbsoluteFile();
        }
      }
    }
  }

  @NonNull
  private static final Pattern IMAGE_PATTERN = Pattern.compile("src=\"([^\"]*)\"");

  @NonNull
  private final Shell f_shell;
  @NonNull
  private final ToolTipImageLoader f_imgLoader;

  /* Whether or not the tool tip is in sticky mode */
  private boolean f_isSticky;
  /* Current tool tip and its text */
  private ToolTipInformationControl f_toolTip;
  private String f_tipText;
  /* The current widget that we are displaying a tool tip for */
  private Widget f_tipWidget;
  /*
   * Whether or not we should be looking for the mouse to exit the tip area.
   */
  boolean f_checkForTipAreaExit;
  /* The position of the current tool tip. */
  private Point f_tipPosition;

  private void statusTip() {
    f_toolTip.dispose();
    f_toolTip = new ToolTipInformationControl(f_shell, I18N.msg("common.tooltip.statusText"));
    f_isSticky = false;
    f_toolTip.setTip(f_tipText);
    f_toolTip.setHoverLocation(f_tipPosition);
    f_toolTip.setVisible(true);
  }

  private void stickyTip() {
    f_toolTip.dispose();
    f_toolTip = new ToolTipInformationControl(f_shell, getToolBar());
    f_isSticky = true;
    f_toolTip.setTip(f_tipText);
    f_toolTip.setHoverLocation(f_tipPosition);
    f_toolTip.setVisible(true);
  }

  private ToolBarManager getToolBar() {
    ToolBarManager man = new ToolBarManager();

    return man;
  }

  public ToolTip(final Shell shell) {
    this(shell, CACHE);
  }

  public ToolTip(final Shell shell, ToolTipImageLoader imgLoader) {
    if (shell == null)
      throw new IllegalArgumentException(I18N.err(44, "shell"));
    f_shell = shell;
    f_imgLoader = imgLoader;
    f_toolTip = new ToolTipInformationControl(shell);
  }

  @Override
  public void mouseDoubleClick(final MouseEvent e) {
    mouseDown(e);
  }

  /*
   * Go sticky if we click the tool tip, otherwise get out of the way.
   */
  @Override
  public void mouseDown(final MouseEvent e) {
    if (f_toolTip.isVisible()) {
      if (f_toolTip.mouseIsOver(e.widget, e.x, e.y)) {
        if (!f_isSticky) {
          stickyTip();
        }
      } else {
        f_toolTip.setVisible(false);
      }
    }
  }

  @Override
  public void mouseUp(final MouseEvent e) {
    // Nothing to do here
  }

  @Override
  public void mouseEnter(final MouseEvent e) {
    // Nothing to do here
  }

  /*
   * Check to see if we are over the tool tip. If we are, then signal that in
   * the future we need to check if the mouse leaves the tool tip. If we aren't,
   * then close the tool tip.
   */
  @Override
  public void mouseExit(final MouseEvent e) {
    if (f_toolTip.isVisible()) {
      if (!f_toolTip.mouseIsOver(e.widget, e.x, e.y)) {
        f_toolTip.setVisible(false);
        f_tipWidget = null;
      } else {
        f_checkForTipAreaExit = true;
      }
    }
  }

  /*
   * Trap hover events to pop-up tooltip
   */
  @Override
  public void mouseHover(final MouseEvent event) {
    final Point pt = new Point(event.x, event.y);
    Widget widget = event.widget;
    if (widget instanceof Control) {
      final Control control = (Control) widget;
      if (widget instanceof ToolBar) {
        final ToolBar w = (ToolBar) widget;
        widget = w.getItem(pt);
      }
      if (widget instanceof Table) {
        final Table w = (Table) widget;
        widget = w.getItem(pt);
      }
      if (widget instanceof Tree) {
        final Tree w = (Tree) widget;
        widget = w.getItem(pt);
      }
      if (widget == null) {
        f_toolTip.setVisible(false);
        f_tipWidget = null;
        return;
      }
      if (widget == f_tipWidget) {
        return;
      }
      f_tipWidget = widget;
      f_tipPosition = control.toDisplay(pt);
      final String text = (String) widget.getData(TIP_TEXT);
      changeTipText(text);
      f_checkForTipAreaExit = false;
      f_tipWidget = widget;
      statusTip();
    }
  }

  private void changeTipText(String text) {
    final StringBuilder buffer = new StringBuilder();
    HTMLPrinter.insertPageProlog(buffer, 0, STYLE_SHEET);
    buffer.append(text);
    HTMLPrinter.addPageEpilog(buffer);
    f_tipText = buffer.toString();
    Matcher m = IMAGE_PATTERN.matcher(text.toString());
    final Set<String> images = new HashSet<String>();
    while (m.find()) {
      images.add(m.group(1));
    }
    for (String image : images) {
      File im = f_imgLoader.getImageFile(image);
      if (im != null) {
        f_tipText = f_tipText.replace(image, im.getAbsolutePath());
      }
    }
  }

  @Override
  public void handleEvent(final Event event) {
    if (event.widget instanceof Control) {
      final Control control = (Control) event.widget;
      // Return if the tool tip is not active.
      if (!f_toolTip.isVisible()) {
        return;
      }
      // We listen to key events here because capturing key events from
      // the control is simply not reliable.
      if (event.type == SWT.KeyUp) {
        if (event.keyCode == SWT.F2 && !f_isSticky) {
          stickyTip();
        } else if (event.keyCode == SWT.ESC) {
          f_toolTip.setVisible(false);
        }
      }
      // Return unless we have exited the control we are monitoring. This
      // event handling mechanism is only for when we have exited the main
      // control, so there is some duplicated logic between this and the
      // listeners that we place on the control.
      if (!f_checkForTipAreaExit || f_tipWidget == null || !(event.widget instanceof Control)) {
        return;
      }
      switch (event.type) {
      case SWT.MouseMove:
      case SWT.MouseEnter:
      case SWT.MouseExit:
        if (!f_isSticky) {
          final Control eventControl = (Control) event.widget;
          if (eventControl == control) {
            return;
          }
          // transform coordinates to subject control:
          final Point mouseLoc = event.display.map(eventControl, control, event.x, event.y);
          if (control.getBounds().contains(mouseLoc)) {
            return;
          }
          if (!f_toolTip.mouseIsOver(event.widget, event.x, event.y)) {
            f_toolTip.setVisible(false);
            f_tipWidget = null;
          }
        }
        break;
      case SWT.MouseDown:
      case SWT.MouseUp:
        final Control eventControl = (Control) event.widget;
        if (eventControl == control) {
          return;
        }
        // transform coordinates to subject control:
        if (!f_toolTip.mouseIsOver(event.widget, event.x, event.y)) {
          f_toolTip.setVisible(false);
          f_tipWidget = null;
        } else {
          if (!f_isSticky) {
            stickyTip();
          }
        }
        break;

      }
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // Do nothing
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (f_toolTip.isVisible()) {
      if (e.keyCode == SWT.F2 && !f_isSticky) {
        stickyTip();
      } else if (e.keyCode == SWT.ESC) {
        f_toolTip.setVisible(false);
      }
    }
  }

  /**
   * Counts successful calls to {@link #register(Control)} so that listeners to
   * the display can be removed when no all registered controls are disposed.
   */
  private int f_registerCount = 0;

  /**
   * Registers the passed control to get pop-up help like the Javadoc pop-up
   * help in the Eclipse Java editor. The control must define {@link #TIP_TEXT}
   * as data on rows or items in the control to set what is shown in the pop-up.
   * <p>
   * When the control is disposed support goes away via a
   * {@link DisposeListener} that is registered by this call.
   * 
   * @param on
   *          the control on which to enable pop-up help.
   */
  public void register(final Control on) {
    if (on == null)
      throw new IllegalArgumentException(I18N.err(44, "on"));

    try {
      Browser browser = new Browser(on.getParent(), SWT.None);
      browser.dispose();
    } catch (SWTError e) {
      SLLogger.getLogger().log(Level.SEVERE, I18N.err(304, ToolTip.class.getName(), on.toString(), Browser.class.getName(), e));
      if (e.code == SWT.ERROR_NO_HANDLES) {
        return;
      } else {
        throw e;
      }
    }

    if (!on.isDisposed()) {
      final Display display = f_shell.getDisplay();
      if (f_registerCount == 0) {
        // only register once at first control registered
        display.addFilter(SWT.Activate, this);
        display.addFilter(SWT.MouseWheel, this);

        display.addFilter(SWT.FocusOut, this);

        display.addFilter(SWT.MouseDown, this);
        display.addFilter(SWT.MouseUp, this);

        display.addFilter(SWT.MouseMove, this);
        display.addFilter(SWT.MouseEnter, this);
        display.addFilter(SWT.MouseExit, this);
        display.addFilter(SWT.KeyUp, this);
      }
      on.addMouseTrackListener(this);
      on.addMouseListener(this);
      on.addKeyListener(this);
      f_registerCount++;
    }
    on.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        f_registerCount--;
        final ToolTip tip = ToolTip.this;
        if (!on.isDisposed()) {
          on.removeMouseTrackListener(tip);
          on.removeMouseListener(tip);
          on.removeKeyListener(tip);
        }
        if (f_registerCount == 0) {
          // no longer need to listen -- no controls registered
          final Display display = f_shell.getDisplay();
          display.removeFilter(SWT.Activate, tip);
          display.removeFilter(SWT.MouseWheel, tip);

          display.removeFilter(SWT.FocusOut, tip);

          display.removeFilter(SWT.MouseDown, tip);
          display.removeFilter(SWT.MouseUp, tip);

          display.removeFilter(SWT.MouseMove, tip);
          display.removeFilter(SWT.MouseEnter, tip);
          display.removeFilter(SWT.MouseExit, tip);
          display.removeFilter(SWT.KeyUp, tip);
        }
      }
    });
  }
}
