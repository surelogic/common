package com.surelogic.flashlight.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.surelogic.common.derby.sqlfunctions.Functions;

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
public class RollupAccessesResultSet implements InvocationHandler {

    private final HappensBeforeAnalysis hb;
    private final ResultSet set;
    private AccessBlock block;
    private AccessBlock next;
    private boolean wasNull;

    RollupAccessesResultSet(Connection conn, ResultSet set, boolean isStatic)
            throws SQLException {
        this.set = set;
        if (set.next()) {
            next = new AccessBlock(new Access(set, isStatic), isStatic);
        }
        hb = new HappensBeforeAnalysis(conn);
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
    public static ResultSet create(Connection conn, ResultSet set,
            boolean isStatic) throws IllegalArgumentException, SQLException {
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

        // This is the last write visible across ALL accesses of this field, not
        // just this thread.
        Timestamp lastWrite;
        long lastWriteThread;

        AccessBlock(Access first, boolean isStatic,
                HappensBeforeState happensBefore, Timestamp lastWrite,
                long lastWriteThread) {
            threadId = first.threadId;
            threadName = first.threadName;
            start = first.ts;
            lastAccess = first.ts;
            if (first.isRead) {
                reads++;
                if (first.underConstruction) {
                    readsUC++;
                }
                this.lastWrite = lastWrite;
                this.lastWriteThread = lastWriteThread;
            } else {
                writes++;
                if (first.underConstruction) {
                    writesUC++;
                }
                this.lastWrite = first.ts;
                this.lastWriteThread = first.threadId;
            }
            this.isStatic = isStatic;
            this.happensBefore = happensBefore;
        }

        AccessBlock(Access first, boolean isStatic) {
            this(first, isStatic, HappensBeforeState.FIRST, null, -1);
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
                        lastWriteThread = a.threadId;
                        writes++;
                        if (a.underConstruction) {
                            writesUC++;
                        }
                    }
                } else {
                    boolean hasHappensBefore = hb.hasHappensBefore(lastWrite,
                            lastWriteThread, a.ts, a.threadId);
                    return new AccessBlock(a, isStatic,
                            hasHappensBefore ? HappensBeforeState.YES
                                    : HappensBeforeState.NO, lastWrite,
                            lastWriteThread);
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

}