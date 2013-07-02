/**
 * 
 */
package com.surelogic.common.derby.sqlfunctions;

import java.util.LinkedList;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;

/**
 * Represents a single line of a stack trace in the Flashlight tool. This class
 * should be available only in flashlight-common, but due to some class loading
 * issues it needs to be in common, i.e., the same project that exports the
 * derby.jar.
 * 
 * @author nathan
 * 
 */
public class Trace {
    final long parentId;
    final long id;
    final String clazz;
    final String clazzCode;
    final String pakkage;
    final String file;
    final String loc;
    final String methodCallClass;
    final String methodCallName;
    final int line;

    Trace(final long id, final long parentId, final String inClass,
            final String classCode, final String inPackage,
            final String inFile, final String location, final int atLine,
            final String methodCallClass, final String methodCallName) {
        this.id = id;
        this.parentId = parentId;
        clazz = inClass;
        clazzCode = classCode;
        pakkage = inPackage;
        file = inFile;
        loc = location;
        line = atLine;
        this.methodCallClass = methodCallClass;
        this.methodCallName = methodCallName;
    }

    public Object get(final int i) {
        switch (i) {
        case 1:
            return clazz;
        case 2:
            return pakkage;
        case 3:
            return file;
        case 4:
            return loc;
        case 5:
            return line;
        case 6:
            return methodCallClass;
        case 7:
            return methodCallName;
        case 8:
            return clazzCode;
        default:
            throw new IllegalArgumentException();
        }
    }

    public long getParentId() {
        return parentId;
    }

    public long getId() {
        return id;
    }

    public String getClazz() {
        return clazz;
    }

    public String getPackage() {
        return pakkage;
    }

    public String getFile() {
        return file;
    }

    public String getLoc() {
        return loc;
    }

    public int getLine() {
        return line;
    }

    public String getMethodCallClass() {
        return methodCallClass;
    }

    public String getMethodCallName() {
        return methodCallName;
    }

    @Override
    public String toString() {
        return pakkage + '.' + clazz + ':' + loc + ':' + line;
    }

    /**
     * Produce a stack trace beginning with the given trace id.
     * 
     * @param traceId
     * @return
     */
    public static DBQuery<LinkedList<Trace>> stackTrace(final long traceId) {
        return new DBQuery<LinkedList<Trace>>() {
            @Override
            public LinkedList<Trace> perform(final Query q) {
                final Queryable<Trace> getTrace = q.prepared(
                        "Trace.selectById",
                        SingleRowHandler.from(new TraceRowHandler()));
                final LinkedList<Trace> traces = new LinkedList<Trace>();
                if (traceId != -1) {
                    Trace t = getTrace.call(traceId);
                    traces.add(t);
                    while (t.parentId != t.id) {
                        t = getTrace.call(t.parentId);
                        traces.add(t);
                    }
                }
                return traces;
            }
        };
    }

    private static class TraceRowHandler implements RowHandler<Trace> {

        @Override
        public Trace handle(final Row r) {
            return new Trace(r.nextLong(), r.nextLong(), r.nextString(),
                    r.nextString(), r.nextString(), r.nextString(),
                    r.nextString(), r.nextInt(), r.nextString(), r.nextString());
        }

    }
}