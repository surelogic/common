package com.surelogic.common.derby.sqlfunctions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.surelogic.common.SLUtility;
import com.surelogic.common.jdbc.BooleanResultHandler;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jdbc.DefaultConnection;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.flashlight.common.HappensBeforeAnalysis;
import com.surelogic.flashlight.common.HappensBeforeAnalysis.HBEdge;
import com.surelogic.flashlight.common.InstanceAccessesResultSet;
import com.surelogic.flashlight.common.RollupAccessesResultSet;
import com.surelogic.flashlight.common.RollupIndirectAccessesResultSet;

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

    private static class LockTraceResultSet implements InvocationHandler {
        private final Iterator<LockTrace> traces;
        private LockTrace trace;

        LockTraceResultSet(final List<LockTrace> traces) {
            this.traces = traces.iterator();
        }

        static ResultSet create(final List<LockTrace> traces) {
            return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                    .getClassLoader(), new Class[] { ResultSet.class },
                    new LockTraceResultSet(traces));
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
            } else if (methodName.startsWith("wasNull")) {
                return false;
            }
            throw new UnsupportedOperationException(method.getName());
        }
    }

    static class TraceHandler implements ResultHandler<String> {

        boolean isStatic;

        TraceHandler(boolean isStatic) {
            this.isStatic = isStatic;
        }

        class CountInfo {
            String thread;
            int reads, writes, readsUC, writesUC;

            CountInfo(String threadName) {
                thread = threadName;
            }

            StringBuilder toString(StringBuilder b) {
                b.append(thread);
                b.append(" (");
                boolean something = false;
                if (reads > 0) {
                    b.append(SLUtility.toStringHumanWithCommas(reads));
                    b.append(" read");
                    if (reads > 1) {
                        b.append('s');
                    }
                    something = true;
                }
                if (writes > 0) {
                    if (something) {
                        b.append(", ");
                    }
                    b.append(SLUtility.toStringHumanWithCommas(writes));
                    b.append(" write");
                    if (writes > 1) {
                        b.append('s');
                    }
                    something = true;
                }
                if (readsUC > 0) {
                    if (something) {
                        b.append(", ");
                    }
                    b.append(SLUtility.toStringHumanWithCommas(readsUC));
                    b.append(" read");
                    if (readsUC > 1) {
                        b.append('s');
                    }
                    b.append(isStatic ? " during class initialization"
                            : "  under construction");
                    something = true;
                }
                if (writesUC > 0) {
                    if (something) {
                        b.append(", ");
                    }
                    b.append(SLUtility.toStringHumanWithCommas(writesUC));
                    b.append(" write");
                    if (writesUC > 1) {
                        b.append('s');
                    }
                    b.append(isStatic ? " during class initialization"
                            : " under construction");
                }
                b.append(")");
                return b;
            }
        }

        @Override
        public String handle(Result result) {
            List<CountInfo> counts = new ArrayList<CountInfo>();
            CountInfo info = null;
            for (Row r : result) {
                String thread = r.nextString();
                if (info == null || !info.thread.equals(thread)) {
                    info = new CountInfo(thread);
                    counts.add(info);
                }
                if ("R".equals(r.nextString())) {
                    if (r.nextBoolean()) {
                        info.readsUC = r.nextInt();
                    } else {
                        info.reads = r.nextInt();
                    }
                } else {
                    if (r.nextBoolean()) {
                        info.writesUC = r.nextInt();
                    } else {
                        info.writes = r.nextInt();
                    }
                }
            }
            StringBuilder b = new StringBuilder();
            for (Iterator<CountInfo> iter = counts.iterator(); iter.hasNext();) {
                CountInfo i = iter.next();
                i.toString(b);
                if (iter.hasNext()) {
                    b.append(", ");
                }
            }
            return b.toString();
        }
    }

    static class LockTraceHandler implements ResultHandler<String> {

        @Override
        public String handle(Result result) {
            StringBuilder b = new StringBuilder();
            for (Row r : result) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                String thread = r.nextString();
                int count = r.nextInt();
                b.append(thread);
                b.append(" (");
                if (count == 1) {
                    b.append("once");
                } else {
                    b.append(SLUtility.toStringHumanWithCommas(count));
                    b.append(" times");
                }
                b.append(')');
            }
            return b.toString();
        }

    }

    public static String coalesceLockEdgeThreads(final long component,
            final long lockHeld, final String lockHeldType,
            final long lockAcquired, final String lockAcquiredType) {
        return DefaultConnection.getInstance().withReadOnly(
                new DBQuery<String>() {
                    @Override
                    public String perform(Query q) {
                        return q.prepared("LockEdge.threads",
                                new LockTraceHandler()).call(component,
                                lockHeld, lockHeldType, lockAcquired,
                                lockAcquiredType);

                    }
                });
    }

    public static String coalesceLockTraceThreads(final long lockTraceId) {
        return DefaultConnection.getInstance().withReadOnly(
                new DBQuery<String>() {
                    @Override
                    public String perform(Query q) {
                        return q.prepared("LockTrace.threads",
                                new LockTraceHandler()).call(lockTraceId);

                    }
                });
    }

    public static String coalesceStaticTraceThreads(final long fieldId,
            final long traceId) {
        return DefaultConnection.getInstance().withReadOnly(
                new DBQuery<String>() {
                    @Override
                    public String perform(Query q) {
                        return q.prepared("Accesses.trace.staticAccessCounts",
                                new TraceHandler(true)).call(fieldId, traceId);
                    }
                });
    }

    public static String coalesceTraceThreads(final long fieldId,
            final long receiverId, final long traceId) {
        return DefaultConnection.getInstance().withReadOnly(
                new DBQuery<String>() {
                    @Override
                    public String perform(Query q) {
                        return q.prepared("Accesses.trace.accessCounts",
                                new TraceHandler(false)).call(fieldId,
                                receiverId, traceId);
                    }
                });
    }

    public static String coalesceAllTraceThreads(final long fieldId,
            final long traceId) {
        return DefaultConnection.getInstance().withReadOnly(
                new DBQuery<String>() {
                    @Override
                    public String perform(Query q) {
                        if (q.prepared("Accesses.isFieldStatic",
                                new BooleanResultHandler()).call(fieldId)) {
                            return q.prepared(
                                    "Accesses.trace.staticAccessCounts",
                                    new TraceHandler(true)).call(fieldId,
                                    traceId);
                        } else {
                            return q.prepared("Accesses.trace.allAccessCounts",
                                    new TraceHandler(false)).call(fieldId,
                                    traceId);
                        }
                    }
                });
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
     * Returns a <em>table</em> representing the lock trace belonging to this
     * trace id.
     * 
     * @param traceId
     * @return
     */
    public static ResultSet lockTrace(final long traceId) {
        return LockTraceResultSet.create(DefaultConnection.getInstance()
                .withDefault(LockTrace.lockTrace(traceId)));
    }

    public static ResultSet objectSummary(long objectId) {
        try {
            // We can't use the framework code here, because we have to leave
            // the result sets open when we return from this block.
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            return RollupIndirectAccessesResultSet.createForObject(conn,
                    objectId);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet staticAccessSummary(long fieldId) {
        try {
            // We can't use the framework code here, because we have to leave
            // the result sets open when we return from this block.
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            return RollupAccessesResultSet.createForStaticField(conn, fieldId);
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
            return RollupAccessesResultSet.createForField(conn, fieldId,
                    receiverId);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet blockSummary(long receiverId, Timestamp start,
            Timestamp stop) throws SQLException {
        try {
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            return InstanceAccessesResultSet.create(conn, receiverId, start,
                    stop);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet fieldsBlockSummary(long receiverId,
            Timestamp start, Timestamp stop, long fieldId, long secondFieldId)
            throws SQLException {
        try {
            Connection conn = DefaultConnection.getInstance()
                    .readOnlyConnection();
            return InstanceAccessesResultSet.create(conn, receiverId, start,
                    stop, fieldId, secondFieldId);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ResultSet happensBeforeEdges(final long writeThread,
            final Timestamp write, final long readThread, final Timestamp read) {
        return DefaultConnection.getInstance().withDefault(
                new DBTransaction<ResultSet>() {

                    @Override
                    public ResultSet perform(Connection conn) throws Exception {
                        HappensBeforeAnalysis hba = new HappensBeforeAnalysis(
                                conn);
                        return HBEdgeResultSet.create(hba.happensBeforeTraces(
                                write, writeThread, read, readThread));
                    }
                });
    }

    static class HBEdgeResultSet implements InvocationHandler {
        private Iterator<HBEdge> edges;
        HBEdge edge;

        HBEdgeResultSet(List<HBEdge> edges) {
            this.edges = edges.iterator();
        }

        static ResultSet create(List<HBEdge> edges) {
            return (ResultSet) Proxy
                    .newProxyInstance(ResultSet.class.getClassLoader(),
                            new Class[] { ResultSet.class },
                            new HBEdgeResultSet(edges));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final String methodName = method.getName();
            if ("next".equals(methodName)) {
                if (edges.hasNext()) {
                    edge = edges.next();
                    return true;
                } else {
                    return false;
                }
            } else if ("close".equals(methodName)) {
                edges = null;
                edge = null;
                return null;
            } else if (methodName.startsWith("get")) {
                return edge.get((Integer) args[0]);
            } else if (methodName.equals("wasNull")) {
                return edge.wasNull();
            }
            throw new UnsupportedOperationException(method.getName());
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
                        String threadName = q.prepared("ObjectId.threadName",
                                new StringResultHandler()).call(id);
                        if (threadName != null) {
                            return threadName;
                        }
                        return q.prepared("ObjectId.selectObjectClass",
                                SingleRowHandler.from(new RowHandler<String>() {
                                    @Override
                                    public String handle(final Row r) {
                                        String clazz = r.nextString();
                                        if (clazz != null) {
                                            return clazz + ".class";
                                        }
                                        clazz = r.nextString();
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
