package com.surelogic.common.adhoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.common.xml.XMLConstants;

public final class AdHocPersistence {

	/*
	 * File formats. We read both but only write the latest.
	 */
	static final String VERSION_1_0 = "1.0";
	static final String VERSION_2_0 = "2.0";
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

	private static final Logger LOG = SLLogger.getLogger();

	/**
	 * Saves the set of queries into an XML file. An example of such a file is:
	 * 
	 * <pre>
	 *  &lt;?xml version='1.0' encoding='UTF-8' standalone='yes'?&gt;
	 *  &lt;ad-hoc version='3.0'&gt;
	 *  &lt;query id='a04dc317-d616-4708-9599-1d7657a57b97' description='show tables'&gt;show tables&lt;/query&gt;
	 *  &lt;query id='47863991-b84d-4624-82e0-9d6e6501341f' description='lockset results'&gt;select * from LOCK&lt;/query&gt;
	 *  &lt;query id='423f68cf-f922-4f6e-b9eb-85a2b436f04b' description='lock details'&gt;select * from LOCK_DETAIL
	 *  where LOCK_ID=?LOCK_ID?&lt;/query&gt;
	 *  &lt;sub-query id='a04dc317-d616-4708-9599-1d7657a57b97' sub-query='47863991-b84d-4624-82e0-9d6e6501341f'/&gt;
	 *  &lt;/ad-hoc&gt;
	 * </pre>
	 * 
	 * @param manager
	 *            a query manager.
	 * @param saveFile
	 *            the file to export queries into.
	 * @param defaultFile
	 *            true if we are saving a new file of defaults, false if we are
	 *            only saving the changed queries
	 */
	public static void save(final AdHocManager manager, final File saveFile,
			final boolean defaultFile) {
		if (defaultFile) {
			save(manager.getQueryList(), saveFile, true);
		} else {
			final List<AdHocQuery> queries = new ArrayList<AdHocQuery>(manager
					.getQueryList());
			for (final Iterator<AdHocQuery> iter = queries.iterator(); iter
					.hasNext();) {
				if (!iter.next().isChanged()) {
					iter.remove();
				}
			}
			save(queries, saveFile, false);
		}
	}

	/**
	 * Saves the passed set of ad hoc queries into a file using a simple XML
	 * format. If there are no queries to be saved, this method ensures that the
	 * save file does not exist.
	 * 
	 * @param revision
	 *            the revision for the full file
	 * @param queries
	 *            the list of queries to export.
	 * @param saveFile
	 *            the file to export queries into.
	 * @param updateRevision
	 *            {@code true} if we should update the revision of any queries
	 *            marked as dirty.
	 */
	public static void save(final List<AdHocQuery> queries,
			final File saveFile, final boolean updateRevision) {
		Collections.sort(queries, new Comparator<AdHocQuery>() {
			public int compare(final AdHocQuery o1, final AdHocQuery o2) {
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
		pw.println("<?xml version='1.0' encoding='" + XMLConstants.ENCODING
				+ "' standalone='yes'?>");
		final StringBuilder b = new StringBuilder();
		b.append("<").append(AD_HOC);
		Entities.addAttribute(VERSION, VERSION_3_0, b);
		b.append(">"); // don't end this element
		pw.println(b.toString());
	}

	private static void outputQuery(final PrintWriter pw,
			final AdHocQuery query, final boolean updateRevision) {
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
			Entities.addAttribute(DISPLAY_AT_ROOT, query
					.showAtRootOfQueryMenu(), b);
		}
		if (!updateRevision && query.isChanged()) {
			Entities.addAttribute(CHANGED, "true", b);
		}
		b.append(">");
		Entities.addEscaped(query.getSql(), b);
		b.append("</").append(QUERY).append(">");
		pw.println(b.toString());
	}

	private static void outputSubQueries(final PrintWriter pw,
			final AdHocQuery query) {
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
	 * Recognized older file formats will read as well as the current file
	 * format version.
	 * 
	 * @param manager
	 *            a query manager.
	 * @param saveFile
	 *            the file to import queries from.
	 * @throws Exception
	 *             if something goes wrong reading the file, e.g., it is not the
	 *             correct format.
	 */
	public static void load(final AdHocManager manager, final File saveFile)
			throws Exception {
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

	public static void load(final AdHocManager manager, final URL url)
			throws Exception {
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
			LOG.warning(String.format(
					"%s cannot be opened to load queries from.", url));
		}
	}

	/**
	 * Loads queries from the passed stream into the passed query manager. This
	 * method merges the new queries with any existing ones within the query
	 * manager.
	 * <p>
	 * The stream will <i>not</i> be closed by this method. Closing the stream
	 * is the responsibility of the client.
	 * <p>
	 * Recognized older file formats will read as well as the current file
	 * format version.
	 * 
	 * @param manager
	 *            a query manager.
	 * @param stream
	 *            the stream to import queries from. This stream will <i>not</i>
	 *            be closed by this method.
	 * @throws Exception
	 *             if something goes wrong reading the file, e.g., it is not the
	 *             correct format.
	 */
	private static void load(final AdHocManager manager,
			final InputStream stream) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final DefaultHandler handler = new AdHocPersistenceReader(manager);
		// Parse the input
		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(stream, handler);
	}
}