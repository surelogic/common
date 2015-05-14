package com.surelogic.common.ui.adhoc.views.doc;

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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.FontData;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.FileUtility;
import com.surelogic.common.ILifecycle;
import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQuery;
import com.surelogic.common.ui.HTMLPrinter;

public class QuerydocMediator extends AdHocManagerAdapter implements ILifecycle {

  private final AdHocManager f_manager;
  private final Browser f_browser;

  public QuerydocMediator(AbstractQuerydocView view, Browser browser) {
    f_manager = view.getManager();
    f_browser = browser;
  }

  @Override
  public void init() {
    showQuerydoc(f_manager.getQueryDoc());
    f_manager.addObserver(this);
  }

  @Override
  public void notifyQuerydocValueChange(AdHocQuery query) {
    showQuerydoc(query);
  }

  @Override
  public void notifyQueryModelChange(AdHocManager manager) {
    /*
     * We update our display here because, perhaps, the text of the Querydoc has
     * changed in the query editor. If not, it only causes extra refreshes when
     * editing queries.
     */
    showQuerydoc(f_manager.getQueryDoc());
  }

  @Override
  public void dispose() {
    f_manager.removeObserver(this);
  }

  public void setFocus() {
    f_browser.setFocus();
  }

  private void showQuerydoc(@Nullable AdHocQuery query) {
    if (query == null) {
      f_browser.setText("<html><body /></html>");
    } else {
      final String querydoc = prepareHtmlText(query.getQueryDoc());
      f_browser.setText(querydoc);
    }
  }

  private String prepareHtmlText(String text) {
    final StringBuilder buffer = new StringBuilder();
    HTMLPrinter.insertPageProlog(buffer, 0, STYLE_SHEET);
    buffer.append(text);
    HTMLPrinter.addPageEpilog(buffer);
    String result = buffer.toString();
    Matcher m = IMAGE_PATTERN.matcher(text.toString());
    final Set<String> images = new HashSet<>();
    while (m.find()) {
      images.add(m.group(1));
    }
    for (String image : images) {
      File im = getImageFile(image);
      if (im != null) {
        result = result.replace(image, im.getAbsolutePath());
      }
    }
    return result;
  }

  private final Map<String, File> fileMap = new HashMap<>();

  public File getImageFile(String imageName) {
    synchronized (fileMap) {
      File f = fileMap.get(imageName);
      if (f == null) {
        final URL gifURL = f_manager.getDataSource().getQuerydocImageURL(imageName);
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

  private static final String STYLE_SHEET_RESOURCE = "/com/surelogic/common/ui/adhoc/views/doc/QuerydocStyleSheet.css";

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
    final URL styleSheetURL = Thread.currentThread().getContextClassLoader().getResource(STYLE_SHEET_RESOURCE);
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
  private static final Pattern IMAGE_PATTERN = Pattern.compile("src=\"([^\"]*)\"");

}
