package com.surelogic.flashlight.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.surelogic.common.jdbc.QB;

public class RollupIndirectAccessesResultSet implements InvocationHandler {

    private final HappensBeforeAnalysis hb;
    private final ResultSet set;
    private AccessBlock block;
    private AccessBlock next;
    private boolean wasNull;

    RollupIndirectAccessesResultSet(Connection conn, ResultSet set,
            long objectId) throws SQLException {
        this.set = set;
        hb = new HappensBeforeAnalysis(conn);
        if (set.next()) {
            next = new AccessBlock(new Access(set));
        }
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

        final HappensBeforeState happensBefore;
        final long threadId;
        final String threadName;
        final Timestamp start;
        Timestamp stop;
        int accesses;
        final Timestamp lastAccess;
        final long lastAccessThread;

        AccessBlock(Access first, HappensBeforeState happensBefore,
                Timestamp lastAccess, long lastAccessThread) {
            threadId = first.threadId;
            threadName = first.threadName;
            start = stop = first.ts;
            accesses++;
            this.lastAccess = lastAccess;
            this.lastAccessThread = lastAccessThread;
            this.happensBefore = happensBefore;
        }

        AccessBlock(Access first) {
            this(first, HappensBeforeState.FIRST, null, -1);
        }

        AccessBlock accumulate() throws SQLException {
            while (set.next()) {
                Access a = new Access(set);
                if (a.threadId == threadId) {
                    stop = a.ts;
                    accesses++;
                } else {

                    boolean hasHappensBefore = hb.hasHappensBefore(stop,
                            threadId, a.ts, a.threadId);
                    return new AccessBlock(a,
                            hasHappensBefore ? HappensBeforeState.YES
                                    : HappensBeforeState.NO, stop, threadId);
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
                return stop;
            case 5:
                return accesses;
            case 6:
                return happensBefore.getDisplay();
            case 7:
                return lastAccess;
            case 8:
                return lastAccessThread;
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

        Access(ResultSet set) throws SQLException {
            int idx = 1;
            threadId = set.getLong(idx++);
            threadName = set.getString(idx++);
            ts = set.getTimestamp(idx++);
        }

        @Override
        public String toString() {
            return "Access [threadId=" + threadId + ", threadName="
                    + threadName + ", ts=" + ts + "]";
        }
    }

    public static ResultSet createForObject(Connection conn, long objectId)
            throws SQLException {
        PreparedStatement st = conn.prepareStatement(QB
                .get("Accesses.selectByObject"));
        st.setLong(1, objectId);
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                .getClassLoader(), new Class[] { ResultSet.class },
                new RollupIndirectAccessesResultSet(conn, st.executeQuery(),
                        objectId));
    }
}
