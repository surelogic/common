/*
 * Created on Dec 7, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.common.derby;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;

import com.surelogic.common.FileUtility;
import com.surelogic.common.jdbc.ISchemaUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * Common methods used by the specific Data classes (FIX only in common-eclipse,
 * because Derby is)
 * 
 * @author chance
 */
public class DerbyDataUtils {
	private static final String JDBC_PRE = "jdbc:derby:";
	private static final String JDBC_POST = ";user="; // + SCHEMA_NAME;

	public static String getConnectionURL(String location, String user) {
		return JDBC_PRE + location + JDBC_POST + user;
	}

	public static void deleteDatabase(final String location) {
		final File dbDir = new File(location);
		if (dbDir.exists()) {
			if (FileUtility.recursiveDelete(dbDir)) {
				SLLogger.getLogger().info(
						"Database deleted at startup : " + location);
			} else {
				SLLogger.getLogger().log(Level.SEVERE,
						"Unable to delete database at startup : " + location);
			}
		}
	}

	public static void createDatabase(ISchemaUtility util, String location,
			String user) throws Exception {
		Derby.bootEmbedded();

		final String connectionURL = getConnectionURL(location, user)
				+ ";create=true";
		final Connection c = DriverManager.getConnection(connectionURL);
		Exception e = null;
		try {
			c.setAutoCommit(false);
			util.checkAndUpdate(c, false);
			c.commit();
		} catch (Exception exc) {
			e = exc;
		} finally {
			try {
				c.close();
			} catch (Exception exc) {
				if (e == null) {
					e = exc;
				}
			}
		}
		if (e != null) {
			throw e;
		}
	}
}
