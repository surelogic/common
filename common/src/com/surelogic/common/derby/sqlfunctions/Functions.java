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

    private static class Access {
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

    private static class AccessBlock {
        final boolean isStatic;
        final long threadId;
        final String threadName;
        final Timestamp start;
        int reads;
        int writes;
        int readsUC;
        int writesUC;
        final HappensBeforeState happensBefore;
        Timestamp lastAccess;
        Timestamp lastWrite;

        AccessBlock(Access first, boolean isStatic,
                HappensBeforeState happensBefore) {
            threadId = first.threadId;
            threadName = first.threadName;
            start = first.ts;
            lastAccess = first.ts;
            if (first.isRead) {
                reads++;
                if (first.underConstruction) {
                    readsUC++;
                }
            } else {
                lastWrite = first.ts;
                writes++;
                if (first.underConstruction) {
                    writesUC++;
                }
            }
            this.isStatic = isStatic;
            this.happensBefore = happensBefore;
        }

        AccessBlock(Access first, boolean isStatic) {
            this(first, isStatic, HappensBeforeState.FIRST);
        }

        AccessBlock accumulate(PreparedStatement hbSt,
                PreparedStatement hbObjSourceSt,
                PreparedStatement hbObjTargetSt, ResultSet set)
                throws SQLException {
            while (set.next()) {
                Access a = new Access(set, isStatic);
                if (a.threadId == threadId) {
                    lastAccess = a.ts;
                    if (a.isRead) {
                        reads++;
                        if (a.underConstruction) {
                            readsUC++;
                        }
                    } else {
                        lastWrite = a.ts;
                        writes++;
                        if (a.underConstruction) {
                            writesUC++;
                        }
                    }
                } else {
                    boolean hasHappensBefore = false;
                    int idx = 1;
                    hbSt.setLong(idx++, threadId);
                    hbSt.setLong(idx++, a.threadId);
                    hbSt.setTimestamp(idx++, lastWrite);
                    hbSt.setTimestamp(idx++, a.ts);
                    final ResultSet hbSet = hbSt.executeQuery();
                    try {
                        // If there is an explicit happens-before, or if no
                        // writes have ever happened we are good
                        if (hbSet.next() || lastWrite == null) {
                            hasHappensBefore = true;
                        } else {
                            // Otherwise we look for more happens-before events
                            idx = 1;
                            hbObjSourceSt.setLong(idx++, threadId);
                            hbObjSourceSt.setTimestamp(idx++, lastWrite);
                            hbObjSourceSt.setTimestamp(idx++, a.ts);
                            final ResultSet hbObjSourceSet = hbObjSourceSt
                                    .executeQuery();
                            try {
                                idx = 1;
                                hbObjTargetSt.setLong(idx++, a.threadId);
                                hbObjTargetSt.setTimestamp(idx++, lastWrite);
                                hbObjTargetSt.setTimestamp(idx++, a.ts);
                                final ResultSet hbObjTargetSet = hbObjTargetSt
                                        .executeQuery();
                                try {
                                    long targetObj = -1;
                                    Timestamp targetTs = null;
                                    sourceLoop: while (hbObjSourceSet.next()) {
                                        long sourceObj = hbObjSourceSet
                                                .getLong(1);
                                        Timestamp sourceTs = hbObjSourceSet
                                                .getTimestamp(2);
                                        if (sourceObj > targetObj) {
                                            while (hbObjTargetSet.next()) {
                                                targetObj = hbObjTargetSet
                                                        .getLong(1);
                                                targetTs = hbObjTargetSet
                                                        .getTimestamp(2);
                                                if (targetObj == sourceObj) {
                                                    if (sourceTs
                                                            .before(targetTs)) {
                                                        hasHappensBefore = true;
                                                        break sourceLoop;
                                                    }
                                                } else if (sourceObj < targetObj) {
                                                    continue sourceLoop;
                                                }
                                            }
                                            // We have exhausted our inner loop,
                                            // we may as well quit
                                            break;
                                        }
                                    }
                                } finally {
                                    hbObjTargetSet.close();
                                }
                            } finally {
                                hbObjSourceSet.close();
                            }
                        }
                    } finally {
                        hbSet.close();
                    }
                    return new AccessBlock(a, isStatic,
                            hasHappensBefore ? HappensBeforeState.YES
                                    : HappensBeforeState.NO);
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
                return lastAccess;
            case 5:
                return reads;
            case 6:
                return writes;
            case 7:
                return happensBefore.getDisplay();
            case 8:
                return readsUC;
            case 9:
                return writesUC;
            default:
                throw new IllegalArgumentException(i
                        + " is not a valid parameter index.");
            }
        }
    }

    private static class RollupAccessesResultSet implements InvocationHandler {

        final PreparedStatement hbSt;
        final PreparedStatement hbObjSourceSt;
        final PreparedStatement hbObjTargetSt;
        final ResultSet set;
        AccessBlock block;
        AccessBlock next;
        boolean wasNull;

        RollupAccessesResultSet(Connection conn, ResultSet set, boolean isStatic)
                throws SQLException {
            hbSt = conn.prepareStatement(QB.get("Accesses.happensBefore"));
            hbObjSourceSt = conn.prepareStatement(QB
                    .get("Accesses.happensBeforeSourceObject"));
            hbObjTargetSt = conn.prepareStatement(QB
                    .get("Accesses.happensBeforeTargetObject"));
            this.set = set;
            if (set.next()) {
                next = new AccessBlock(new Access(set, isStatic), isStatic);
            }
        }

        static ResultSet create(Connection conn, ResultSet set, boolean isStatic)
                throws IllegalArgumentException, SQLException {
            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class[] { ResultSet.class },
                    new RollupAccessesResultSet(conn, set, isStatic));
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
                next = block
                        .accumulate(hbSt, hbObjSourceSt, hbObjTargetSt, set);
                return true;
            } else if ("close".equals(methodName)) {
                set.close();
                hbSt.close();
                hbObjSourceSt.close();
                hbObjTargetSt.close();
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
