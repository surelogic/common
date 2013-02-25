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
import com.surelogic.common.ILifecycle;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.HTMLPrinter;

/**
 * Enables tool tip support on tables, trees, and other controls.
 * 
 * @author nathan
 * 
 */
public class ToolTip {

  public static final String TIP_TEXT = "TIP_TEXT";

  @Nullable
  private static final String styleSheet = getStyleSheet();

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
  private static final TemporaryFileImageCache cache = new TemporaryFileImageCache();

  /**
   * Creates temporary files so that third party sources (such as browsers) can
   * view the files located in {@link CommonImages}.
   * 
   * @author nathan
   * 
   */
  private static class TemporaryFileImageCache implements ImageLoader {

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
  private static final Pattern imagePattern = Pattern.compile("src=\"([^\"]*)\"");

  @NonNull
  private final Shell shell;
  @NonNull
  private final ImageLoader imgLoader;

  public ToolTip(final Shell parent) {
    this(parent, cache);
  }

  public ToolTip(final Shell shell, ImageLoader imgLoader) {
    if (shell == null)
      throw new IllegalArgumentException(I18N.err(44, "shell"));
    this.shell = shell;
    this.imgLoader = imgLoader;
  }

  public interface ImageLoader {
    File getImageFile(String gifName);
  }

  private class ToolTipWatcher implements ILifecycle, KeyListener, MouseListener, MouseTrackListener, Listener {
    /* The control that we are watching */
    private final Control control;
    private final Display display;

    /* Whether or not the tool tip is in sticky mode */
    private boolean isSticky;
    /* Current tool tip and its text */
    private ToolTipInformationControl toolTip;
    private String tipText;
    /* The current widget that we are displaying a tool tip for */
    private Widget tipWidget;
    /*
     * Whether or not we should be looking for the mouse to exit the tip area.
     */
    boolean checkForTipAreaExit;
    /* The position of the current tool tip. */
    private Point tipPosition;

    private void statusTip() {
      toolTip.dispose();
      toolTip = new ToolTipInformationControl(shell, I18N.msg("common.tooltip.statusText"));
      isSticky = false;
      toolTip.setTip(tipText);
      toolTip.setHoverLocation(tipPosition);
      toolTip.setVisible(true);
    }

    private void stickyTip() {
      toolTip.dispose();
      toolTip = new ToolTipInformationControl(shell, getToolBar());
      isSticky = true;
      toolTip.setTip(tipText);
      toolTip.setHoverLocation(tipPosition);
      toolTip.setVisible(true);
    }

    private ToolBarManager getToolBar() {
      ToolBarManager man = new ToolBarManager();

      return man;
    }

    ToolTipWatcher(final Control control) {
      if (control == null)
        throw new IllegalArgumentException(I18N.err(44, "control"));
      this.control = control;
      this.display = control.getDisplay();
      toolTip = new ToolTipInformationControl(shell);
    }

    public void init() {
      if (!display.isDisposed()) {
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
      if (!control.isDisposed()) {
        control.addMouseTrackListener(this);
        control.addMouseListener(this);
        control.addKeyListener(this);
      }
    }

    public void dispose() {
      if (!control.isDisposed()) {
        control.removeMouseTrackListener(this);
        control.removeMouseListener(this);
        control.removeKeyListener(this);
      }
      if (!display.isDisposed()) {
        display.removeFilter(SWT.Activate, this);
        display.removeFilter(SWT.MouseWheel, this);

        display.removeFilter(SWT.FocusOut, this);

        display.removeFilter(SWT.MouseDown, this);
        display.removeFilter(SWT.MouseUp, this);

        display.removeFilter(SWT.MouseMove, this);
        display.removeFilter(SWT.MouseEnter, this);
        display.removeFilter(SWT.MouseExit, this);
        display.removeFilter(SWT.KeyUp, this);
      }
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
      if (toolTip.isVisible()) {
        if (toolTip.mouseIsOver(e.widget, e.x, e.y)) {
          if (!isSticky) {
            stickyTip();
          }
        } else {
          toolTip.setVisible(false);
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
     * the future we need to check if the mouse leaves the tool tip. If we
     * aren't, then close the tool tip.
     */
    @Override
    public void mouseExit(final MouseEvent e) {
      if (toolTip.isVisible()) {
        if (!toolTip.mouseIsOver(e.widget, e.x, e.y)) {
          toolTip.setVisible(false);
          tipWidget = null;
        } else {
          checkForTipAreaExit = true;
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
        toolTip.setVisible(false);
        tipWidget = null;
        return;
      }
      if (widget == tipWidget) {
        return;
      }
      tipWidget = widget;
      tipPosition = control.toDisplay(pt);
      final String text = (String) widget.getData(TIP_TEXT);
      changeTipText(text);
      checkForTipAreaExit = false;
      tipWidget = widget;
      statusTip();
    }

    private void changeTipText(String text) {
      final StringBuilder buffer = new StringBuilder();
      HTMLPrinter.insertPageProlog(buffer, 0, styleSheet);
      buffer.append(text);
      HTMLPrinter.addPageEpilog(buffer);
      tipText = buffer.toString();
      Matcher m = imagePattern.matcher(text.toString());
      final Set<String> images = new HashSet<String>();
      while (m.find()) {
        images.add(m.group(1));
      }
      for (String image : images) {
        File im = imgLoader.getImageFile(image);
        if (im != null) {
          tipText = tipText.replace(image, im.getAbsolutePath());
        }
      }
    }

    @Override
    public void handleEvent(final Event event) {
      // Return if the tool tip is not active.
      if (!toolTip.isVisible()) {
        return;
      }
      // We listen to key events here because capturing key events from
      // the control is simply not reliable.
      if (event.type == SWT.KeyUp) {
        if (event.keyCode == SWT.F2 && !isSticky) {
          stickyTip();
        } else if (event.keyCode == SWT.ESC) {
          toolTip.setVisible(false);
        }
      }
      // Return unless we have exited the control we are monitoring. This
      // event handling mechanism is only for when we have exited the main
      // control, so there is some duplicated logic between this and the
      // listeners that we place on the control.
      if (!checkForTipAreaExit || tipWidget == null || !(event.widget instanceof Control)) {
        return;
      }
      switch (event.type) {
      case SWT.MouseMove:
      case SWT.MouseEnter:
      case SWT.MouseExit:
        if (!isSticky) {
          final Control eventControl = (Control) event.widget;
          if (eventControl == control) {
            return;
          }
          // transform coordinates to subject control:
          final Point mouseLoc = event.display.map(eventControl, control, event.x, event.y);
          if (control.getBounds().contains(mouseLoc)) {
            return;
          }
          if (!toolTip.mouseIsOver(event.widget, event.x, event.y)) {
            toolTip.setVisible(false);
            tipWidget = null;
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
        if (!toolTip.mouseIsOver(event.widget, event.x, event.y)) {
          toolTip.setVisible(false);
          tipWidget = null;
        } else {
          if (!isSticky) {
            stickyTip();
          }
        }
        break;

      }
    }

    @Override
    public void keyPressed(KeyEvent e) {
      // Do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
      if (toolTip.isVisible()) {
        if (e.keyCode == SWT.F2 && !isSticky) {
          stickyTip();
        } else if (e.keyCode == SWT.ESC) {
          toolTip.setVisible(false);
        }
      }
    }
  }

  /**
   * Enables customized hover help for a specified control
   * 
   * @param on
   *          the control on which to enable hover help.
   */
  public void activateToolTip(final Control on) {
    try {
      Browser browser = new Browser(on.getParent(), SWT.None);
      browser.dispose();
    } catch (SWTError e) {
      if (e.code == SWT.ERROR_NO_HANDLES) {
        // If we don't have a Browser, we just won't initialize the
        // ToolTip
        return;
      } else {
        throw e;
      }
    }
    final ToolTipWatcher ttw = new ToolTipWatcher(on);
    ttw.init();
    on.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        ttw.dispose();
      }
    });
  }
}
