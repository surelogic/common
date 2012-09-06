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
        // No instaqnce
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

    private static class Access {
        // A.INTHREAD, THO.THREADNAME , A.TS, A.RW
        long threadId;
        String threadName;
        Timestamp ts;
        boolean isRead;

        Access(ResultSet set) throws SQLException {
            int idx = 1;
            threadId = set.getLong(idx++);
            threadName = set.getString(idx++);
            ts = set.getTimestamp(idx++);
            isRead = set.getString(idx++).equals("R");
        }

    }

    private static class AccessBlock {
        long threadId;
        String threadName;
        Timestamp start;
        Timestamp end;
        int reads;
        int writes;

        AccessBlock(Access first) {
            threadId = first.threadId;
            threadName = first.threadName;
            start = first.ts;
            end = first.ts;
            if (first.isRead) {
                reads++;
            } else {
                writes++;
            }
        }

        AccessBlock accumulate(ResultSet set) throws SQLException {
            while (set.next()) {
                Access a = new Access(set);
                if (a.threadId == threadId) {
                    end = a.ts;
                    if (a.isRead) {
                        reads++;
                    } else {
                        writes++;
                    }
                } else {
                    return new AccessBlock(a);
                }
            }
            return null;
        }

        Object get(int i) {
            switch (i) {
            case 1:
                return threadId;
            case 2:
                return threadName;
            case 3:
                return start;
            case 4:
                return end;
            case 5:
                return reads;
            case 6:
                return writes;
            default:
                throw new IllegalArgumentException(i
                        + " is not a valid parameter index.");
            }
        }
    }

    private static class RollupAccessesResultSet implements InvocationHandler {

        ResultSet set;
        AccessBlock block;
        AccessBlock next;
        boolean wasNull;

        RollupAccessesResultSet(ResultSet set) throws SQLException {
            this.set = set;
            if (set.next()) {
                next = new AccessBlock(new Access(set));
            }
        }

        static ResultSet create(ResultSet set) throws IllegalArgumentException,
                SQLException {
            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class[] { ResultSet.class },
                    new RollupAccessesResultSet(set));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final String methodName = method.getName();
            if ("next".equals(methodName)) {
                if (next == null) {
                    return false;
                }
                block = next;
                next = block.accumulate(set);
                return true;
            } else if ("close".equals(methodName)) {
                set.close();
                return null;
            } else if (methodName.startsWith("get")) {
                Object o = block.get((Integer) args[0]);
                wasNull = o == null;
                return o;
            } else if (methodName.equals("wasNull")) {
                return wasNull;
            }
            throw new UnsupportedOperationException(method.getName());
        }

    }

    public static ResultSet staticAccessSummary(long fieldId) {
        try {
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            PreparedStatement st = conn.prepareStatement(QB
                    .get("Accesses.selectByField"));
            st.setLong(1, fieldId);
            return RollupAccessesResultSet.create(st.executeQuery());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet accessSummary(long fieldId, long receiverId)
            throws SQLException {
        try {
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            PreparedStatement st = conn.prepareStatement(QB
                    .get("Accesses.selectByFieldAndReceiver"));
            st.setLong(1, fieldId);
            st.setLong(2, receiverId);
            return RollupAccessesResultSet.create(st.executeQuery());
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
