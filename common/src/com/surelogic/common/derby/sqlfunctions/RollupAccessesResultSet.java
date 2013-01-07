package com.surelogic.common.derby.sqlfunctions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.surelogic.common.derby.sqlfunctions.Functions.Access;
import com.surelogic.common.derby.sqlfunctions.Functions.HappensBeforeState;
import com.surelogic.common.jdbc.QB;

/**
 * An implementation of ResultSet for use with
 * {@link Functions#staticAccessSummary(long)} and
 * {@link Functions#accessSummary(long, long)}. It produces a summary of access
 * blocks along with accompanying information on whether or not a happens-before
 * relationship exists between contiguous access blocks.
 * 
 * @author nathan
 * 
 */
class RollupAccessesResultSet implements InvocationHandler {

    final PreparedStatement hbSt;
    final PreparedStatement hbObjSourceSt;
    final PreparedStatement hbObjTargetSt;
    final PreparedStatement hbCollSourceSt;
    final PreparedStatement hbCollTargetSt;
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
        hbCollSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceColl"));
        hbCollTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetColl"));
        this.set = set;
        if (set.next()) {
            next = new AccessBlock(new Access(set, isStatic), isStatic);
        }
    }

    /**
     * 
     * @param conn
     * @param set
     *            a result set consisting of all accesses of this field, with
     *            the columns being INTHREAD, THREADNAME, TS, RW, and
     *            UNDERCONSTRUCTION for non-static fields
     * @param isStatic
     *            whether or not the field is static
     * @return
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    static ResultSet create(Connection conn, ResultSet set, boolean isStatic)
            throws IllegalArgumentException, SQLException {
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                .getClassLoader(), new Class[] { ResultSet.class },
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
            next = block.accumulate();
            return true;
        } else if ("close".equals(methodName)) {
            set.close();
            hbSt.close();
            hbObjSourceSt.close();
            hbObjTargetSt.close();
            hbCollSourceSt.close();
            hbCollTargetSt.close();
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

    private class AccessBlock {
        final boolean isStatic;

        final HappensBeforeState happensBefore;
        final long threadId;
        final String threadName;
        final Timestamp start;

        int reads;
        int writes;
        int readsUC;
        int writesUC;

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

        private boolean happensBeforeThread(Access a) throws SQLException {
            int idx = 1;
            hbSt.setLong(idx++, threadId);
            hbSt.setLong(idx++, a.threadId);
            hbSt.setTimestamp(idx++, lastWrite);
            hbSt.setTimestamp(idx++, a.ts);
            final ResultSet hbSet = hbSt.executeQuery();
            try {
                return hbSet.next();
            } finally {
                hbSet.close();
            }
        }

        private boolean happensBeforeCollection(Access a) throws SQLException {
            int idx = 1;
            hbCollSourceSt.setLong(idx++, threadId);
            hbCollSourceSt.setTimestamp(idx++, lastWrite);
            hbCollSourceSt.setTimestamp(idx++, a.ts);
            final ResultSet hbCollSourceSet = hbObjSourceSt.executeQuery();
            try {
                idx = 1;
                hbCollTargetSt.setLong(idx++, a.threadId);
                hbCollTargetSt.setTimestamp(idx++, lastWrite);
                hbCollTargetSt.setTimestamp(idx++, a.ts);
                final ResultSet hbCollTargetSet = hbObjTargetSt.executeQuery();
                try {
                    long targetColl = -1;
                    long targetObj = -1;
                    Timestamp targetTs = null;
                    sourceLoop: while (hbCollSourceSet.next()) {
                        long sourceColl = hbCollSourceSet.getLong(1);
                        long sourceObj = hbCollSourceSet.getLong(2);
                        Timestamp sourceTs = hbCollSourceSet.getTimestamp(3);
                        if (sourceObj > targetObj && sourceColl > targetColl) {
                            while (hbCollTargetSet.next()) {
                                targetColl = hbCollTargetSet.getLong(1);
                                targetObj = hbCollTargetSet.getLong(2);
                                targetTs = hbCollTargetSet.getTimestamp(3);
                                if (targetObj == sourceObj
                                        && targetColl == sourceColl) {
                                    if (sourceTs.before(targetTs)) {
                                        return true;
                                    }
                                } else if (sourceObj < targetObj
                                        || sourceColl < targetColl) {
                                    continue sourceLoop;
                                }
                            }
                            // We have exhausted our inner loop, we may as well
                            // quit
                            break;
                        }
                    }
                    return false;
                } finally {
                    hbCollTargetSet.close();
                }
            } finally {
                hbCollSourceSet.close();
            }
        }

        private boolean happensBeforeObject(Access a) throws SQLException {
            int idx = 1;
            hbObjSourceSt.setLong(idx++, threadId);
            hbObjSourceSt.setTimestamp(idx++, lastWrite);
            hbObjSourceSt.setTimestamp(idx++, a.ts);
            final ResultSet hbObjSourceSet = hbObjSourceSt.executeQuery();
            try {
                idx = 1;
                hbObjTargetSt.setLong(idx++, a.threadId);
                hbObjTargetSt.setTimestamp(idx++, lastWrite);
                hbObjTargetSt.setTimestamp(idx++, a.ts);
                final ResultSet hbObjTargetSet = hbObjTargetSt.executeQuery();
                try {
                    long targetObj = -1;
                    Timestamp targetTs = null;
                    sourceLoop: while (hbObjSourceSet.next()) {
                        long sourceObj = hbObjSourceSet.getLong(1);
                        Timestamp sourceTs = hbObjSourceSet.getTimestamp(2);
                        if (sourceObj > targetObj) {
                            while (hbObjTargetSet.next()) {
                                targetObj = hbObjTargetSet.getLong(1);
                                targetTs = hbObjTargetSet.getTimestamp(2);
                                if (targetObj == sourceObj) {
                                    if (sourceTs.before(targetTs)) {
                                        return true;
                                    }
                                } else if (sourceObj < targetObj) {
                                    continue sourceLoop;
                                }
                            }
                            // We have exhausted our inner loop, we may as well
                            // quit
                            break;
                        }
                    }
                    return false;
                } finally {
                    hbObjTargetSet.close();
                }
            } finally {
                hbObjSourceSet.close();
            }
        }

        AccessBlock accumulate() throws SQLException {
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
                    boolean hasHappensBefore = lastWrite == null
                            || happensBeforeThread(a) || happensBeforeObject(a)
                            || happensBeforeCollection(a);

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
}