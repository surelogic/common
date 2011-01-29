package com.surelogic.common.adhoc;

import static com.surelogic.common.adhoc.AdHocPersistence.AD_HOC;
import static com.surelogic.common.adhoc.AdHocPersistence.CHANGED;
import static com.surelogic.common.adhoc.AdHocPersistence.DEFAULT_SUB_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.DESCRIPTION;
import static com.surelogic.common.adhoc.AdHocPersistence.DISPLAY;
import static com.surelogic.common.adhoc.AdHocPersistence.DISPLAY_AT_ROOT;
import static com.surelogic.common.adhoc.AdHocPersistence.ID;
import static com.surelogic.common.adhoc.AdHocPersistence.QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.REVISION;
import static com.surelogic.common.adhoc.AdHocPersistence.SUB_QUERY;
import static com.surelogic.common.adhoc.AdHocPersistence.VERSION;
import static com.surelogic.common.adhoc.AdHocPersistence.VERSION_1_0;
import static com.surelogic.common.adhoc.AdHocPersistence.VERSION_2_0;
import static com.surelogic.common.adhoc.AdHocPersistence.VERSION_3_0;

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

	private String f_version = null;

	private AdHocQuery f_query = null;

	private StringBuilder f_sql = null;

	/*
	 * Ignore list for queries that occur more than once but aren't actually
	 * newer.
	 */
	private final Set<String> f_subQueryIgnoreList = new HashSet<String>();

	@Override
	public void startElement(final String uri, final String localName,
			final String name, final Attributes attributes) throws SAXException {
		if (name.equals(AD_HOC)) {
			final String fileVersion = attributes.getValue(VERSION);
			if (VERSION_1_0.equals(fileVersion)) {
				f_version = VERSION_1_0;
			} else if (VERSION_2_0.equals(fileVersion)) {
				f_version = VERSION_2_0;
			} else if (VERSION_3_0.equals(fileVersion)) {
				f_version = VERSION_3_0;
			} else {
				throw new SAXException(I18N.err(122, fileVersion));
			}
		} else {
			/*
			 * Version 1.0 file format
			 */
			if (VERSION_1_0.equals(f_version)) {
				// Do nothing, we are using the 3.0 system now
			}
			/*
			 * Version 2.0 file format
			 */
			if (VERSION_2_0.equals(f_version)) {
				// Do nothing, we are using the 3.0 system now
			}

			/*
			 * Version 3.0 file format
			 */
			if (VERSION_3_0.equals(f_version)) {
				if (name.equals(QUERY)) {
					final String queryId = attributes.getValue(ID);
					if (!"".equals(queryId)) {
						f_query = f_manager.get(queryId);
						final long revision = Long.parseLong(attributes
								.getValue(REVISION));
						if (f_query.getRevision() <= revision) {
							f_subQueryIgnoreList.remove(queryId);
							f_sql = new StringBuilder();
							f_query.setRevision(Long.parseLong(attributes
									.getValue(REVISION)));
							final String description = attributes
									.getValue(DESCRIPTION);
							if (!"".equals(description)) {
								f_query.setDescription(description);
							}
							final String displayString = attributes
									.getValue(DISPLAY);
							if (!"".equals(displayString)) {
								f_query.setShowInQueryMenu(Boolean
										.parseBoolean(displayString));
							}
							final String changed = attributes.getValue(CHANGED);
							f_query
									.setChanged("true"
											.equalsIgnoreCase(changed));
							final String displayAtRootString = attributes
									.getValue(DISPLAY_AT_ROOT);
							if (!"".equals(displayAtRootString)) {
								f_query.setShowAtRootOfQueryMenu(Boolean
										.parseBoolean(displayAtRootString));
							}
							// Remove any extant subqueries. New ones will be
							// added at the end of this file
							for (final AdHocQuery q : f_query.getSubQueries()) {
								f_query.removeSubQuery(q);
							}
						} else {
							// We last parsed an older version of this query, so
							// don't process it's subqueries.
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
					 * The below logic assumes that all query definitions come
					 * before any sub-query definitions.
					 */
					final String queryId = attributes.getValue(ID);
					final String subQueryId = attributes.getValue(SUB_QUERY);
					if (!"".equals(queryId) && !"".equals(subQueryId)) {
						if (!f_subQueryIgnoreList.contains(queryId)) {
							final AdHocQuery query = f_manager.get(queryId);
							final AdHocQuery subQuery = f_manager
									.get(subQueryId);
							query.addSubQuery(subQuery);

							final String isDefault = attributes
									.getValue(DEFAULT_SUB_QUERY);
							if ("true".equalsIgnoreCase(isDefault))
								query.setDefaultSubQuery(subQuery);
						}
					}
				}
			}
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		/*
		 * Same logic for version 1.0 file format and version 2.0 file format.
		 */
		if (f_sql != null) {
			final String text = String.copyValueOf(ch, start, length);
			f_sql.append(text);
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String name) throws SAXException {
		/*
		 * Same logic for version 1.0 file format and version 2.0 file format.
		 */
		if (QUERY.equals(name) && f_query != null && f_sql != null) {
			f_query.setSql(f_sql.toString());
			f_query = null;
			f_sql = null;
		}
	}
}
