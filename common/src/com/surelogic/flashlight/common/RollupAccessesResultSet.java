package com.surelogic.flashlight.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.surelogic.common.derby.sqlfunctions.Functions;
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
public class RollupAccessesResultSet implements InvocationHandler {

    private final HappensBeforeAnalysis hb;
    private final ResultSet set;
    private AccessBlock block;
    private AccessBlock next;
    private boolean wasNull;

    RollupAccessesResultSet(Connection conn, ResultSet set, boolean isStatic,
            long fieldId) throws SQLException {
        this.set = set;
        hb = new HappensBeforeAnalysis(conn);
        if (set.next()) {
            next = new AccessBlock(new Access(set, isStatic), isStatic,
                    hb.happensBeforeFinal(fieldId));
        }
    }

    public static ResultSet create(Connection conn, long fieldId,
            long receiverId) throws SQLException {
        PreparedStatement st = conn.prepareStatement(QB
                .get("Accesses.selectByFieldAndReceiver"));
        st.setLong(1, fieldId);
        st.setLong(2, receiverId);
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                .getClassLoader(), new Class[] { ResultSet.class },
                new RollupAccessesResultSet(conn, st.executeQuery(), false,
                        fieldId));
    }

    public static ResultSet create(Connection conn, long fieldId)
            throws SQLException {
        PreparedStatement st = conn.prepareStatement(QB
                .get("Accesses.selectByField"));
        st.setLong(1, fieldId);
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                .getClassLoader(), new Class[] { ResultSet.class },
                new RollupAccessesResultSet(conn, st.executeQuery(), true,
                        fieldId));
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
            hb.finished();
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
        final boolean isFinal;

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
        // just this thread. If the first access of this block is itself a write
        // then we set it to that.
        final Timestamp lastWrite;
        final long lastWriteThread;

        Timestamp nextWrite;
        long nextWriteThread;

        AccessBlock(Access first, boolean isStatic, boolean isFinal,
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
                this.lastWrite = nextWrite = lastWrite;
                this.lastWriteThread = nextWriteThread = lastWriteThread;
            } else {
                writes++;
                if (first.underConstruction) {
                    writesUC++;
                }
                this.lastWrite = nextWrite = first.ts;
                this.lastWriteThread = nextWriteThread = first.threadId;
            }
            this.isStatic = isStatic;
            this.isFinal = isFinal;
            this.happensBefore = happensBefore;
        }

        AccessBlock(Access first, boolean isStatic, boolean isFinal) {
            this(first, isStatic, isFinal, HappensBeforeState.FIRST, null, -1);
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
                        nextWrite = a.ts;
                        nextWriteThread = a.threadId;
                        writes++;
                        if (a.underConstruction) {
                            writesUC++;
                        }
                    }
                } else {
                    boolean hasHappensBefore = !a.isRead
                            || hb.hasHappensBefore(nextWrite, nextWriteThread,
                                    a.ts, a.threadId);
                    return new AccessBlock(a, isStatic, isFinal, isFinal
                            || hasHappensBefore ? HappensBeforeState.YES
                            : HappensBeforeState.NO, nextWrite, nextWriteThread);
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
                return lastWrite == null ? new Timestamp(0) : lastWrite;
            case 9:
                return lastWriteThread;
            case 10:
                return readsUC;
            case 11:
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

        @Override
        public String toString() {
            return "Access [threadId=" + threadId + ", threadName="
                    + threadName + ", ts=" + ts + ", isRead=" + isRead
                    + ", underConstruction=" + underConstruction + "]";
        }

    }

}