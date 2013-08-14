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
            long traceId, String packageName, String className,
            String classCode, String location, String locationCode, int atLine) {
        this.lockTraceId = lockTraceId;
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
            return packageName;
        case 4:
            return className;
        case 5:
            return classCode;
        case 6:
            return location;
        case 7:
            return locationCode;
        case 8:
            return atLine;
        case 9:
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
                    r.nextLong(), r.nextString(), r.nextString(),
                    r.nextString(), r.nextString(), r.nextString(), r.nextInt());
        }

    }
}
