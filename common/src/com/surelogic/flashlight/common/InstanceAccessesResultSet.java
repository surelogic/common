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

public class InstanceAccessesResultSet implements InvocationHandler {

    private boolean wasNull;
    private final ResultSet set;
    private AccessBlock block;
    private AccessBlock next;

    private InstanceAccessesResultSet(ResultSet set) throws SQLException {
        this.set = set;
        if (set.next()) {
            next = new AccessBlock(new Access(set));
        }
    }

    public static ResultSet create(Connection conn, long receiverId,
            Timestamp begin, Timestamp end) throws SQLException {
        PreparedStatement st = conn.prepareStatement(QB
                .get("Accesses.prep.selectAccessesInBlock"));
        st.setLong(1, receiverId);
        st.setTimestamp(2, begin);
        st.setTimestamp(3, end);
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class
                .getClassLoader(), new Class[] { ResultSet.class },
                new InstanceAccessesResultSet(st.executeQuery()));
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
        final long fieldId;
        final String fieldName;
        final String fieldCode;
        final long threadId;
        final String threadName;
        final Timestamp start, stop;
        int reads, writes, readsUC, writesUC;

        AccessBlock(Access first) {
            fieldId = first.fieldId;
            fieldName = first.fieldName;
            fieldCode = first.fieldCode;
            threadId = first.threadId;
            threadName = first.threadName;
            start = first.ts;
            stop = first.ts;
            if (first.isRead) {
                reads++;
                if (first.underConstruction) {
                    readsUC++;
                }
            } else {
                writes++;
                if (first.underConstruction) {
                    writesUC++;
                }
            }
        }

        AccessBlock accumulate() throws SQLException {
            while (set.next()) {
                Access a = new Access(set);
                if (a.threadId == threadId && a.fieldId == fieldId) {
                    if (a.isRead) {
                        reads++;
                        if (a.underConstruction) {
                            readsUC++;
                        }
                    } else {
                        writes++;
                        if (a.underConstruction) {
                            writesUC++;
                        }
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
                return fieldId;
            case 2:
                return fieldName;
            case 3:
                return fieldCode;
            case 4:
                return threadId;
            case 5:
                return threadName;
            case 6:
                return start;
            case 7:
                return stop;
            case 8:
                return reads;
            case 9:
                return writes;
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
        final long fieldId;
        final String fieldName;
        final String fieldCode;
        final long threadId;
        final String threadName;
        final Timestamp ts;
        final boolean isRead;
        final boolean underConstruction;

        Access(ResultSet set) throws SQLException {
            int idx = 1;
            fieldId = set.getLong(idx++);
            fieldName = set.getString(idx++);
            fieldCode = set.getString(idx++);
            threadId = set.getLong(idx++);
            threadName = set.getString(idx++);
            ts = set.getTimestamp(idx++);
            isRead = set.getString(idx++).equals("R");
            underConstruction = set.getString(idx++).equals("Y");
        }

    }
}
