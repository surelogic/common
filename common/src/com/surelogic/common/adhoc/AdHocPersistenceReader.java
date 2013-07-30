package com.surelogic.common.adhoc;

import static com.surelogic.common.adhoc.AdHocPersistence.AD_HOC;
import static com.surelogic.common.adhoc.AdHocPersistence.CATEGORY;
import static com.surelogic.common.adhoc.AdHocPersistence.CAT_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.CHANGED;
import static com.surelogic.common.adhoc.AdHocPersistence.CUSTOM_DISPLAY;
import static com.surelogic.common.adhoc.AdHocPersistence.DEFAULT_SUB_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.DESCRIPTION;
import static com.surelogic.common.adhoc.AdHocPersistence.DISPLAY;
import static com.surelogic.common.adhoc.AdHocPersistence.DISPLAY_AT_ROOT;
import static com.surelogic.common.adhoc.AdHocPersistence.HAS_DATA;
import static com.surelogic.common.adhoc.AdHocPersistence.ID;
import static com.surelogic.common.adhoc.AdHocPersistence.NO_DATA;
import static com.surelogic.common.adhoc.AdHocPersistence.NO_DEFAULT_SUB_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.REVISION;
import static com.surelogic.common.adhoc.AdHocPersistence.SORT_HINT;
import static com.surelogic.common.adhoc.AdHocPersistence.SUB_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.TYPE;
import static com.surelogic.common.adhoc.AdHocPersistence.VERSION;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.i18n.I18N;

/**
 * SAX reader for ad hoc query XML files. Version 1.0 files and version 2.0
 * files are supported.
 */
public final class AdHocPersistenceReader extends DefaultHandler {

  private final AdHocManager f_manager;

  AdHocPersistenceReader(final AdHocManager manager) {
    f_manager = manager;
  }

  private AdHocQuery f_query = null;

  private AdHocCategory f_category = null;

  private StringBuilder f_sql = null;

  /*
   * Ignore list for queries that occur more than once but aren't actually
   * newer.
   */
  private final Set<String> f_subQueryIgnoreList = new HashSet<String>();

  @Override
  public void startElement(final String uri, final String localName, final String name, final Attributes attributes)
      throws SAXException {
    if (name.equals(AD_HOC)) {
      final String fileVersion = attributes.getValue(VERSION);
      if (AdHocPersistence.checkIfFileVersionIsTooOldToRead(fileVersion)) {
        throw new SAXException(I18N.err(122, fileVersion));
      }
    } else if (name.equals(QUERY)) {
      final String queryId = attributes.getValue(ID);
      if (!"".equals(queryId)) {
        f_query = f_manager.getOrCreateQuery(queryId);
        final long revision = Long.parseLong(attributes.getValue(REVISION));
        if (f_query.getRevision() <= revision) {
          f_subQueryIgnoreList.remove(queryId);
          f_sql = new StringBuilder();
          f_query.setRevision(revision);
          final String description = attributes.getValue(DESCRIPTION);
          if (!"".equals(description)) {
            f_query.setDescription(description);
          }
          final String displayString = attributes.getValue(DISPLAY);
          if (!"".equals(displayString)) {
            f_query.setShowInQueryMenu(Boolean.parseBoolean(displayString));
          }
          final String noDefaultSubQueryString = attributes.getValue(NO_DEFAULT_SUB_QUERY);
          if (!"".equals(noDefaultSubQueryString)) {
            f_query.setNoDefaultSubQuery(Boolean.parseBoolean(noDefaultSubQueryString));
          }
          final String customDisplay = attributes.getValue(CUSTOM_DISPLAY);
          if (customDisplay != null) {
            f_query.setCustomDisplayClassName(customDisplay);
          }
          final String sortHintString = attributes.getValue(SORT_HINT);
          if (sortHintString != null) {
            f_query.setSortHint(Integer.parseInt(sortHintString));
          }
          final String typeString = attributes.getValue(TYPE);
          if (typeString != null) {
            f_query.setType(AdHocQueryType.valueOf(typeString));
          }
          final String changed = attributes.getValue(CHANGED);
          f_query.setChanged("true".equalsIgnoreCase(changed));
          final String displayAtRootString = attributes.getValue(DISPLAY_AT_ROOT);
          if (!"".equals(displayAtRootString)) {
            f_query.setShowAtRootOfQueryMenu(Boolean.parseBoolean(displayAtRootString));
          }
          // Remove any extant subqueries. New ones will be
          // added at the end of this file
          f_query.clearSubQueries();
        } else {
          // We last parsed an newer version of this query, so
          // don't process it's subqueries in this file.
          f_subQueryIgnoreList.add(queryId);
          f_query = null;
          f_sql = null;
        }
      } else {
        f_query = null;
        f_sql = null;
      }
    } else if (name.equals(SUB_QUERY)) {
      /*
       * The below logic assumes that all query definitions come before any
       * sub-query definitions.
       */
      final String queryId = attributes.getValue(ID);
      final String subQueryId = attributes.getValue(SUB_QUERY);
      if (!"".equals(queryId) && !"".equals(subQueryId)) {
        if (!f_subQueryIgnoreList.contains(queryId)) {
          final AdHocQuery query = f_manager.getOrCreateQuery(queryId);
          final AdHocQuery subQuery = f_manager.getOrCreateQuery(subQueryId);
          int isDefaultValue = 0; // default priority for default sub-query
          final String isDefault = attributes.getValue(DEFAULT_SUB_QUERY);
          if ("true".equalsIgnoreCase(isDefault)) {
            // obsolete scheme of "true" to mark static default -- change to 10
            isDefaultValue = 10;
          } else {
            try {
              // new scheme is a priority
              isDefaultValue = Integer.parseInt(isDefault);
            } catch (Exception ignore) {
              // ignore
            }
          }
          query.addSubQuery(subQuery, isDefaultValue);
        }
      }
    } else if (name.equals(CATEGORY)) {
      /*
       * The below logic assumes that all query and sub-query definitions come
       * before any category definitions.
       */
      final String categoryId = attributes.getValue(ID);
      if (!"".equals(categoryId)) {
        f_category = f_manager.getOrCreateCategory(categoryId);
        final long revision = Long.parseLong(attributes.getValue(REVISION));
        if (f_category.getRevision() <= revision) {
          f_category.setRevision(revision);
          final String description = attributes.getValue(DESCRIPTION);
          if (!"".equals(description)) {
            f_category.setDescription(description);
          }
          final String hasDataText = attributes.getValue(HAS_DATA);
          if (!"".equals(hasDataText)) {
            f_category.setHasDataText(hasDataText);
          }
          final String noDataText = attributes.getValue(NO_DATA);
          if (!"".equals(noDataText)) {
            f_category.setNoDataText(noDataText);
          }
          final String sortHintString = attributes.getValue(SORT_HINT);
          if (sortHintString != null) {
            f_category.setSortHint(Integer.parseInt(sortHintString));
          }
          final String changed = attributes.getValue(CHANGED);
          f_category.setChanged("true".equalsIgnoreCase(changed));
          // Remove any extant queries. New ones will be added
          f_category.clearQueries();
        } else {
          // We last parsed an newer version of this category, so
          // don't process it's queries from this file.
          f_category = null;
        }
      } else {
        f_category = null;
      }
    } else if (name.equals(CAT_QUERY)) {
      /*
       * These links are nested within a category definition.
       */
      if (f_category != null) {
        final String queryId = attributes.getValue(ID);
        final AdHocQuery query = f_manager.getQueryOrNull(queryId);
        if (query != null)
          f_category.addQuery(query);
      }
    }
  }

  @Override
  public void characters(final char[] ch, final int start, final int length) throws SAXException {
    if (f_sql != null) {
      final String text = String.copyValueOf(ch, start, length);
      f_sql.append(text);
    }
  }

  @Override
  public void endElement(final String uri, final String localName, final String name) throws SAXException {
    if (QUERY.equals(name) && f_query != null && f_sql != null) {
      f_query.setSql(f_sql.toString());
      f_query = null;
      f_sql = null;
    } else if (CATEGORY.equals(name) && f_category != null) {
      f_category = null;
    }
  }
}
