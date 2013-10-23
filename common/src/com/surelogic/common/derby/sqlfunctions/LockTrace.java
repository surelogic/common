package com.surelogic.common.derby.sqlfunctions;

import java.util.LinkedList;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;

public class LockTrace {
    final long lockTraceId;
    final LockType type;
    final long parentId;
    final long lockId;
    final String packageName;
    final String className;
    final String classCode;
    final String location;
    final String locationCode;
    final int atLine;
    final long traceId;

    private LockTrace(long lockTraceId, long parentId, long lockId,
            long traceId, LockType type, String packageName, String className,
            String classCode, String location, String locationCode, int atLine) {
        this.lockTraceId = lockTraceId;
        this.type = type;
        this.parentId = parentId;
        this.lockId = lockId;
        this.packageName = packageName;
        this.className = className;
        this.classCode = classCode;
        this.location = location;
        this.locationCode = locationCode;
        this.atLine = atLine;
        this.traceId = traceId;
    }

    public Object get(int col) {
        switch (col) {
        case 1:
            return lockId;
        case 2:
            return traceId;
        case 3:
            return type.toString();
        case 4:
            return packageName;
        case 5:
            return className;
        case 6:
            return classCode;
        case 7:
            return location;
        case 8:
            return locationCode;
        case 9:
            return atLine;
        case 10:
            return lockTraceId;
        default:
            throw new IllegalArgumentException();
        }
    }

    public long getLockId() {
        return lockId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getLocation() {
        return location;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public int getAtLine() {
        return atLine;
    }

    public long getTraceId() {
        return traceId;
    }

    /**
     * Produce a stack trace beginning with the given trace id.
     * 
     * @param traceId
     * @return
     */
    public static DBQuery<LinkedList<LockTrace>> lockTrace(final long traceId) {
        return new DBQuery<LinkedList<LockTrace>>() {
            @Override
            public LinkedList<LockTrace> perform(final Query q) {
                final Queryable<LockTrace> getTrace = q.prepared(
                        "LockTrace.selectById",
                        SingleRowHandler.from(new LockTraceRowHandler()));
                final LinkedList<LockTrace> traces = new LinkedList<LockTrace>();
                if (traceId != -1) {
                    LockTrace t = getTrace.call(traceId);
                    traces.add(t);
                    while (t.parentId != t.lockTraceId) {
                        t = getTrace.call(t.parentId);
                        traces.add(t);
                    }
                }
                return traces;
            }
        };
    }

    private static class LockTraceRowHandler implements RowHandler<LockTrace> {

        @Override
        public LockTrace handle(Row r) {
            return new LockTrace(r.nextLong(), r.nextLong(), r.nextLong(),
                    r.nextLong(), LockType.fromFlag(r.nextString()),
                    r.nextString(), r.nextString(), r.nextString(),
                    r.nextString(), r.nextString(), r.nextInt());
        }

    }

    enum LockType {
        INTRINSIC("I", "Intrinsic lock"), UTIL("U", "java.util.concurrent lock");

        private final String flag;
        private final String desc;

        LockType(String flag, String desc) {
            this.flag = flag;
            this.desc = desc;
        }

        public String getFlag() {
            return flag;
        }

        static LockType fromFlag(String flag) {
            for (LockType t : values()) {
                if (t.flag.equals(flag)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Not a valid flag.");
        }

        @Override
        public String toString() {
            return desc;
        }
    }
}
