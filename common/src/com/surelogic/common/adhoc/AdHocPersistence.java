package com.surelogic.common.adhoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;

public final class AdHocPersistence {

  /*
   * File formats. We read both but only write the latest.
   */
  static final String VERSION_3_0 = "3.0";
  static final String AD_HOC = "ad-hoc";
  static final String CHANGED = "changed";
  static final String DEFAULT_SUB_QUERY = "is-default";
  static final String DESCRIPTION = "description";
  static final String DISPLAY = "display";
  static final String DISPLAY_AT_ROOT = "display-at-root";
  static final String ID = "id";
  static final String QUERY = "query";
  static final String SUB_QUERY = "sub-query";
  static final String VERSION = "version";
  static final String REVISION = "revision";
  static final String CATEGORY = "category";
  static final String HAS_DATA = "has-data";
  static final String NO_DATA = "no-data";
  static final String CAT_QUERY = "cat-query";

  private static final Logger LOG = SLLogger.getLogger();

  /**
   * Saves the set of queries into an XML file. A flag indicates if all queries
   * or just changed queries are output.
   * 
   * @param manager
   *          a query manager.
   * @param saveFile
   *          the file to export into.
   */
  public static void exportDiffFile(final AdHocManager manager, final File saveFile) {
    final List<AdHocQuery> queries = manager.getQueryList();
    for (final Iterator<AdHocQuery> iter = queries.iterator(); iter.hasNext();) {
      if (!iter.next().isChanged()) {
        iter.remove();
      }
    }
    final List<AdHocCategory> categories = manager.getCategoryList();
    for (final Iterator<AdHocCategory> iter = categories.iterator(); iter.hasNext();) {
      if (!iter.next().isChanged()) {
        iter.remove();
      }
    }
    export(queries, categories, saveFile, false);
  }

  /**
   * Used to create a new default file (the revision is incremented for changed
   * queries and categories) of queries and categories. Optionally, a list of
   * specific queries may be passed&mdash;all of which must be owned by the
   * passed query manager&mdash;to remove some queries from the new default file
   * (such as test or debug queries). If this list is {@code null} then all
   * queries owned by the manager are put into the file.
   * 
   * @param manager
   *          a query manager.
   * @param queries
   *          an optional set of queries to output, {@code null} means to output
   *          all of <tt>manager</tt>'s queries.
   * @param saveFile
   *          the file to export into.
   */
  public static void exportDefaultFile(final AdHocManager manager, @Nullable List<AdHocQuery> queries, final File saveFile) {
    if (queries == null)
      queries = manager.getQueryList();
    export(queries, manager.getCategoryList(), saveFile, true);
  }

  /**
   * Saves the passed set of ad hoc queries and categories into a file using a
   * simple XML format. If there are no queries to be saved, this method ensures
   * that the save file does not exist. (Categories without any queries makes no
   * sense and will also result in no save file.)
   * 
   * @param queries
   *          the list of queries to export.
   * @param categories
   *          the list of categories to export
   * @param saveFile
   *          the file to export into.
   * @param updateRevision
   *          {@code true} if we should update the revision of any queries or
   *          categories marked as changed.
   */
  private static void export(final List<AdHocQuery> queries, final List<AdHocCategory> categories, final File saveFile,
      final boolean updateRevision) {
    Collections.sort(queries, new Comparator<AdHocQuery>() {
      @Override
      public int compare(final AdHocQuery o1, final AdHocQuery o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });
    Collections.sort(categories, new Comparator<AdHocCategory>() {
      @Override
      public int compare(final AdHocCategory o1, final AdHocCategory o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });
    if (queries == null || queries.isEmpty()) {
      if (saveFile.exists()) {
        saveFile.delete();
      }
      return;
    }
    try {
      final PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
      outputXMLHeader(pw);
      for (final AdHocQuery query : queries) {
        outputQuery(pw, query, updateRevision);
      }
      for (final AdHocQuery query : queries) {
        outputSubQueries(pw, query);
      }
      for (final AdHocCategory category : categories) {
        outputCategory(pw, category, updateRevision, queries);
      }
      outputXMLFooter(pw);
      pw.close();
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine(I18N.msg("adhoc.save", saveFile));
      }
    } catch (final IOException e) {
      LOG.log(Level.SEVERE, I18N.err(3, saveFile), e);
    }
  }

  private static void outputXMLHeader(final PrintWriter pw) {
    pw.println("<?xml version='1.0' encoding='" + SLUtility.ENCODING + "' standalone='yes'?>");
    final StringBuilder b = new StringBuilder();
    b.append("<").append(AD_HOC);
    Entities.addAttribute(VERSION, VERSION_3_0, b);
    b.append(">"); // don't end this element
    pw.println(b.toString());
  }

  private static void outputQuery(final PrintWriter pw, final AdHocQuery query, final boolean updateRevision) {
    final StringBuilder b = new StringBuilder();
    b.append("  <").append(QUERY);
    Entities.addAttribute(ID, query.getId(), b);
    Entities.addAttribute(DESCRIPTION, query.getDescription(), b);
    long revision = query.getRevision();
    if (updateRevision && query.isChanged())
      revision++;
    Entities.addAttribute(REVISION, Long.toString(revision), b);
    if (query.showInQueryMenu()) {
      Entities.addAttribute(DISPLAY, query.showInQueryMenu(), b);
    }
    if (query.showAtRootOfQueryMenu()) {
      Entities.addAttribute(DISPLAY_AT_ROOT, query.showAtRootOfQueryMenu(), b);
    }
    if (!updateRevision && query.isChanged()) {
      Entities.addAttribute(CHANGED, "true", b);
    }
    b.append(">");
    Entities.addEscaped(query.getSql(), b);
    b.append("</").append(QUERY).append(">");
    pw.println(b.toString());
  }

  private static void outputCategory(final PrintWriter pw, final AdHocCategory category, final boolean updateRevision,
      List<AdHocQuery> queries) {
    final StringBuilder b = new StringBuilder();
    b.append("  <").append(CATEGORY);
    Entities.addAttribute(ID, category.getId(), b);
    Entities.addAttribute(DESCRIPTION, category.getDescription(), b);
    Entities.addAttribute(HAS_DATA, category.getHasDataText(), b);
    Entities.addAttribute(NO_DATA, category.getNoDataText(), b);
    long revision = category.getRevision();
    if (updateRevision && category.isChanged())
      revision++;
    Entities.addAttribute(REVISION, Long.toString(revision), b);
    if (!updateRevision && category.isChanged()) {
      Entities.addAttribute(CHANGED, "true", b);
    }
    b.append(">\n");
    for (AdHocQuery query : category.getQueries()) {
      /*
       * Only output a link if it is in the list of queries we are exporting.
       */
      if (queries.contains(query)) {
        b.append("    <").append(CAT_QUERY);
        Entities.addAttribute(ID, query.getId(), b);
        b.append("/>\n");
      }
    }
    b.append("</").append(CATEGORY).append(">");
    pw.println(b.toString());
  }

  private static void outputSubQueries(final PrintWriter pw, final AdHocQuery query) {
    for (final AdHocQuery subQuery : query.getSubQueries()) {
      final StringBuilder b = new StringBuilder();
      b.append("  <").append(SUB_QUERY);
      Entities.addAttribute(ID, query.getId(), b);
      Entities.addAttribute(SUB_QUERY, subQuery.getId(), b);
      if (query.isDefaultSubQuery(subQuery)) {
        Entities.addAttribute(DEFAULT_SUB_QUERY, true, b);
      }
      b.append("/>");
      pw.println(b.toString());
    }
  }

  private static void outputXMLFooter(final PrintWriter pw) {
    pw.println("</" + AD_HOC + ">");
  }

  /**
   * Loads queries from the passed file into the passed query manager. This
   * method merges the new queries with any existing ones within the query
   * manager.
   * <p>
   * Recognized older file formats will read as well as the current file format
   * version.
   * 
   * @param manager
   *          a query manager.
   * @param saveFile
   *          the file to import queries from.
   * @throws Exception
   *           if something goes wrong reading the file, e.g., it is not the
   *           correct format.
   */
  public static void load(final AdHocManager manager, final File saveFile) throws Exception {
    final InputStream stream = new FileInputStream(saveFile);
    try {
      load(manager, stream);
    } finally {
      stream.close();
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine(I18N.msg("adhoc.load", saveFile));
    }
  }

  public static void load(final AdHocManager manager, final URL url) throws Exception {
    final InputStream stream = url.openStream();
    if (stream != null) {
      try {
        load(manager, stream);
      } finally {
        stream.close();
      }
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine(I18N.msg("adhoc.load", url));
      }
    } else {
      LOG.warning(String.format("%s cannot be opened to load queries from.", url));
    }
  }

  /**
   * Loads queries from the passed stream into the passed query manager. This
   * method merges the new queries with any existing ones within the query
   * manager.
   * <p>
   * The stream will <i>not</i> be closed by this method. Closing the stream is
   * the responsibility of the client.
   * <p>
   * Recognized older file formats will read as well as the current file format
   * version.
   * 
   * @param manager
   *          a query manager.
   * @param stream
   *          the stream to import queries from. This stream will <i>not</i> be
   *          closed by this method.
   * @throws Exception
   *           if something goes wrong reading the file, e.g., it is not the
   *           correct format.
   */
  private static void load(final AdHocManager manager, final InputStream stream) throws Exception {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final DefaultHandler handler = new AdHocPersistenceReader(manager);
    // Parse the input
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(stream, handler);
  }
}
