package com.surelogic.common.derby.sqlfunctions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DefaultConnection;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;

public final class Functions {

	private Functions() {
		// No instance
	}

	private static class TraceResultSet implements InvocationHandler {
		private final Iterator<Trace> traces;
		private Trace trace;

		TraceResultSet(final List<Trace> traces) {
			this.traces = traces.iterator();
		}

		static ResultSet create(final List<Trace> traces) {
			return (ResultSet) Proxy
					.newProxyInstance(ResultSet.class.getClassLoader(),
							new Class[] { ResultSet.class },
							new TraceResultSet(traces));
		}

		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable {
			final String methodName = method.getName();
			if ("next".equals(methodName)) {
				if (traces.hasNext()) {
					trace = traces.next();
					return true;
				} else {
					return false;
				}
			} else if ("close".equals(methodName)) {
				while (traces.hasNext()) {
					traces.next();
				}
				trace = null;
				return null;
			} else if (methodName.startsWith("get")) {
				return trace.get((Integer) args[0]);
			}
			throw new UnsupportedOperationException(method.getName());
		}
	}

	/**
	 * Returns a <em>table</em> representing the stack trace belonging to this
	 * trace id.
	 * 
	 * @param traceId
	 * @return
	 */
	public static ResultSet stackTrace(final long traceId) {
		return TraceResultSet.create(DefaultConnection.getInstance()
				.withDefault(Trace.stackTrace(traceId)));
	}

	/**
	 * Returns the actual lock id when passed an id from the lock table. This
	 * will often be the same as the id passed in, but will be different if the
	 * lock is a r/w lock.
	 * 
	 * @param id
	 * @return
	 */
	public static long lockId(final long id) {
		return DefaultConnection.getInstance().withDefault(new DBQuery<Long>() {
			public Long perform(final Query q) {
				return q.prepared("LockId.selectRWLock",
						new ResultHandler<Long>() {
							public Long handle(final Result result) {
								for (final Row r : result) {
									return r.nextLong();
								}
								return id;
							}
						}).call(id);
			}
		});
	}

	/**
	 * Returns a more human-readable representation of the object with the given
	 * id.
	 * 
	 * @param id
	 * @return
	 */
	public static String objId(final long id) {
		return DefaultConnection.getInstance().withDefault(
				new DBQuery<String>() {
					public String perform(final Query q) {
						return q.prepared("ObjectId.selectClass",
								SingleRowHandler.from(new RowHandler<String>() {
									public String handle(final Row r) {
										String clazz = r.nextString();
										if (clazz.length() > 50) {
											clazz = clazz.substring(0, 50);
										}
										return clazz + "-" + id;
									}
								})).call(id);
					}
				});
	}
}
