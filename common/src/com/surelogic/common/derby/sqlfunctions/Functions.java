package com.surelogic.common.derby.sqlfunctions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DefaultConnection;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;

/**
 * Some useful functions used in the Flashlight tool. This class should be
 * available only in flashlight-common, but due to some class loading issues it
 * needs to be in common, i.e., the same project that exports the derby.jar.
 * 
 * @author nathan
 * 
 */
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

        @Override
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

    static class Access {
        // A.INTHREAD, THO.THREADNAME , A.TS, A.RW
        final long threadId;
        final String threadName;
        final Timestamp ts;
        final boolean isRead;
        final boolean underConstruction;

        Access(ResultSet set, boolean isStatic) throws SQLException {
            int idx = 1;
            threadId = set.getLong(idx++);
            threadName = set.getString(idx++);
            ts = set.getTimestamp(idx++);
            isRead = set.getString(idx++).equals("R");
            underConstruction = !isStatic && set.getString(idx++).equals("Y");
        }

    }

    enum HappensBeforeState {
        FIRST(" "), YES("Yes"), NO("No");
        private final String display;

        HappensBeforeState(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

    }

    public static ResultSet staticAccessSummary(long fieldId) {
        try {
            // We can't use the framework code here, because we have to leave
            // the result sets open when we return from this block.
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            PreparedStatement st = conn.prepareStatement(QB
                    .get("Accesses.selectByField"));
            st.setLong(1, fieldId);
            return RollupAccessesResultSet
                    .create(conn, st.executeQuery(), true);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet accessSummary(long fieldId, long receiverId)
            throws SQLException {
        try {
            // We can't use the framework code here, because we have to leave
            // the result sets open when we return from this block.
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            PreparedStatement st = conn.prepareStatement(QB
                    .get("Accesses.selectByFieldAndReceiver"));
            st.setLong(1, fieldId);
            st.setLong(2, receiverId);
            return RollupAccessesResultSet.create(conn, st.executeQuery(),
                    false);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
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
            @Override
            public Long perform(final Query q) {
                return q.prepared("LockId.selectRWLock",
                        new ResultHandler<Long>() {
                            @Override
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
                    @Override
                    public String perform(final Query q) {
                        return q.prepared("ObjectId.selectClass",
                                SingleRowHandler.from(new RowHandler<String>() {
                                    @Override
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
