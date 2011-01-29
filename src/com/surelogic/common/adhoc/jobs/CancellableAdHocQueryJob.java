package com.surelogic.common.adhoc.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.apache.derby.impl.jdbc.EmbedConnection;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocQueryFullyBound;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.AdHocQueryResultEmpty;
import com.surelogic.common.adhoc.AdHocQueryResultSqlData;
import com.surelogic.common.adhoc.AdHocQueryResultSqlException;
import com.surelogic.common.adhoc.AdHocQueryResultSqlUpdateCount;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.ResultSetUtility;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.serviceability.UsageMeter;

public class CancellableAdHocQueryJob extends AbstractSLJob {

	/**
	 * The fully bound query that this job will run on the database.
	 */
	private final AdHocQueryFullyBound f_query;

	/**
	 * The query result that provided variables to allow {@link #f_query} to be
	 * fully bound. This will become the parent result for the query result
	 * created by this job. This field may be {@code null}.
	 */
	private final AdHocQueryResultSqlData f_parentResultOrNull;
	/**
	 * The access key that this query should be run under.
	 */
	private final String[] f_accessKeys;
	private volatile Connection f_connection;

	/**
	 * Constructs a job to run a query on the database.
	 * <p>
	 * When the job completes it will notify the query's manager about the new
	 * result.
	 * 
	 * @param queryToRun
	 *            the query to run. Cannot be {@code null}.
	 * @param parentResultOrNull
	 *            the result directly used to define variables for the query to
	 *            be run. Cannot be {@code null}
	 * 
	 * @throws NullPointerException
	 *             if {@code queryToRun==null}.
	 */
	public CancellableAdHocQueryJob(final AdHocQueryFullyBound queryToRun,
			final AdHocQueryResultSqlData parentResult) {
		super(I18N.msg("adhoc.jobs.name", queryToRun.getQuery()
				.getDescription()));
		if (parentResult == null) {
			throw new IllegalArgumentException(I18N.err(44, "parentResult"));
		}
		f_query = queryToRun;
		f_parentResultOrNull = parentResult;
		f_accessKeys = parentResult.getAccessKeys();
	}

	/**
	 * Constructs a job to run a query on the database.
	 * <p>
	 * When the job completes it will notify the query's manager about the new
	 * result.
	 * 
	 * @param queryToRun
	 *            the query to run. Cannot be {@code null}.
	 * @param the
	 *            access key this query should be run under. Use this
	 *            constructor when no prior result exists.
	 * 
	 * @throws NullPointerException
	 *             if {@code queryToRun==null}.
	 */
	public CancellableAdHocQueryJob(final AdHocQueryFullyBound queryToRun,
			final String... accessKeys) {
		super(I18N.msg("adhoc.jobs.name", queryToRun.getQuery()
				.getDescription()));
		f_query = queryToRun;
		f_parentResultOrNull = null;
		f_accessKeys = accessKeys;
	}

	public SLStatus run(final SLProgressMonitor monitor) {
		try {
			final AdHocManager manager = f_query.getManager();
			monitor.begin();
			final DBConnection datasource = f_parentResultOrNull == null ? manager
					.getDataSource().getDB()
					: f_parentResultOrNull.getDB();
			final SLLicenseProduct product = datasource.getSchemaLoader()
					.getProduct();
			final SLStatus failed = SLLicenseUtility.validateSLJob(product,
					monitor);
			if (failed != null) {
				return failed;
			}

			UsageMeter.getInstance().tickUse(
					"AdHocQuery ran query '"
							+ f_query.getQuery().getDescription() + "'");

			try {
				AdHocQueryResult queryResult;
				final String sql = f_query.getSql();
				try {
					f_connection = datasource.getConnection();
					try {
						final Statement st = f_connection.createStatement();
						try {
							if (monitor.isCanceled()) {
								return SLStatus.CANCEL_STATUS;
							}

							final boolean hasResultSet = st.execute(sql);
							if (SLLogger.getLogger().isLoggable(Level.FINE)) {
								SLLogger.getLogger().fine(
										I18N.msg("adhoc.query", sql));
							}

							if (monitor.isCanceled()) {
								return SLStatus.CANCEL_STATUS;
							}

							if (hasResultSet) {
								// result set
								final ResultSet rs = st.getResultSet();
								try {
									final ResultSetUtility.Result results = ResultSetUtility
											.getResult(rs, f_query.getQuery()
													.getManager()
													.getDataSource()
													.getMaxRowsPerQuery());
									if (results.rows.length == 0) {
										queryResult = new AdHocQueryResultEmpty(
												manager, f_parentResultOrNull,
												f_query, datasource);
									} else {
										if (f_parentResultOrNull == null) {
											queryResult = new AdHocQueryResultSqlData(
													manager, f_query, results,
													datasource, f_accessKeys);
										} else {
											queryResult = new AdHocQueryResultSqlData(
													manager,
													f_parentResultOrNull,
													f_query, results,
													datasource);
										}
									}
								} catch (final Exception e) {
									/*
									 * Unlike an SQLException, which indicates
									 * the query was bad, this is actually a bug
									 * so we fail out of the job.
									 */
									final int code = 6;
									return SLStatus.createErrorStatus(code,
											I18N.err(code, sql), e);
								} finally {
									rs.close();
								}
							} else {
								// update count or no results
								final int updateCount = st.getUpdateCount();
								queryResult = new AdHocQueryResultSqlUpdateCount(
										manager, f_parentResultOrNull, f_query,
										updateCount, datasource);
							}
						} finally {
							st.close();
						}
					} finally {
						f_connection.close();
					}
				} catch (final SQLException e) {
					queryResult = new AdHocQueryResultSqlException(f_query
							.getManager(), f_parentResultOrNull, f_query, e,
							datasource);
				}
				datasource.shutdown();

				manager.notifyResultModelChange();
				manager.setSelectedResult(queryResult);
			} finally {
				monitor.done();
			}
			return SLStatus.OK_STATUS;
		} finally {
			f_done = true;
		}
	}

	public void cancel() {
		((EmbedConnection) f_connection).cancelRunningStatement();
	}

	private volatile boolean f_done;

	public boolean isDone() {
		return f_done;
	}
}
