package com.surelogic.flashlight.common;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.surelogic.common.jdbc.QB;

public class HappensBeforeAnalysis {

    private final PreparedStatement hbSt;
    private final PreparedStatement hbVolWriteSt;
    private final PreparedStatement hbVolReadSt;
    private final PreparedStatement hbObjSourceSt;
    private final PreparedStatement hbObjTargetSt;
    private final PreparedStatement hbCollSourceSt;
    private final PreparedStatement hbCollTargetSt;
    private final PreparedStatement hbLockSourceSt;
    private final PreparedStatement hbLockTargetSt;
    private final PreparedStatement hbClassInitSt;

    private final PreparedStatement hbTraceSt;
    private final PreparedStatement hbVolWriteTraceSt;
    private final PreparedStatement hbVolReadTraceSt;
    private final PreparedStatement hbObjSourceTraceSt;
    private final PreparedStatement hbObjTargetTraceSt;
    private final PreparedStatement hbLockSourceTraceSt;
    private final PreparedStatement hbLockTargetTraceSt;
    private final PreparedStatement hbCollSourceTraceSt;
    private final PreparedStatement hbCollTargetTraceSt;
    private final PreparedStatement hbClassInitTraceSt;

    private final PreparedStatement isFinalSt;

    private final TLongObjectMap<Timestamp> targetsCache;
    private final TLongObjectMap<Timestamp> sourcesCache;

    public HappensBeforeAnalysis(Connection conn) throws SQLException {
        hbSt = conn.prepareStatement(QB.get("Accesses.happensBefore"));
        isFinalSt = conn.prepareStatement(QB.get("Accesses.isFieldFinal"));
        hbVolReadSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeVolatileRead"));
        hbVolWriteSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeVolatileWrite"));
        hbObjSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceObject"));
        hbObjTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetObject"));
        hbLockSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceLock"));
        hbLockTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetLock"));
        hbCollSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceColl"));
        hbCollTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetColl"));
        hbClassInitSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeClassInit"));
        hbTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBefore"));
        hbVolReadTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeVolatileRead"));
        hbVolWriteTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeVolatileWrite"));
        hbObjSourceTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeSourceObject"));
        hbObjTargetTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeTargetObject"));
        hbCollSourceTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeSourceColl"));
        hbCollTargetTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeTargetColl"));
        hbLockSourceTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeSourceLock"));
        hbLockTargetTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeTargetLock"));
        hbClassInitTraceSt = conn.prepareStatement(QB
                .get("Accesses.trace.happensBeforeClassInit"));
        targetsCache = new TLongObjectHashMap<Timestamp>();
        sourcesCache = new TLongObjectHashMap<Timestamp>();
    }

    public void finished() throws SQLException {
        hbSt.close();
        hbObjSourceSt.close();
        hbObjTargetSt.close();
        hbCollSourceSt.close();
        hbCollTargetSt.close();
        hbVolReadSt.close();
        hbVolWriteSt.close();
        hbLockSourceSt.close();
        hbLockTargetSt.close();
        hbTraceSt.close();
        hbObjSourceTraceSt.close();
        hbObjTargetTraceSt.close();
        hbCollSourceTraceSt.close();
        hbCollTargetTraceSt.close();
        hbVolReadTraceSt.close();
        hbVolWriteTraceSt.close();
        hbLockSourceTraceSt.close();
        hbLockTargetTraceSt.close();
        isFinalSt.close();
    }

    public boolean happensBeforeFinal(long fieldId) throws SQLException {
        isFinalSt.setLong(1, fieldId);
        ResultSet set = isFinalSt.executeQuery();
        try {
            set.next();
            return set.getString(1).equals("Y");
        } finally {
            set.close();
        }
    }

    public boolean happensBefore(PreparedStatement sourceSt,
            PreparedStatement targetSt, Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        sourceSt.setLong(1, writeThread);
        sourceSt.setTimestamp(2, write);
        sourceSt.setTimestamp(3, read);
        final ResultSet sourceSet = sourceSt.executeQuery();
        try {
            if (!sourceSet.next()) {
                return false;
            }
            targetSt.setLong(1, readThread);
            targetSt.setTimestamp(2, write);
            targetSt.setTimestamp(3, read);
            final ResultSet targetSet = targetSt.executeQuery();
            try {
                if (!targetSet.next()) {
                    return false;
                }
                final TLongObjectMap<Timestamp> sources = genSources(sourceSet);
                sourceSet.close();
                if (sources.isEmpty()) {
                    return false;
                }
                final TLongObjectMap<Timestamp> targets = genTargets(targetSet);
                targetSet.close();
                if (targets.isEmpty()) {
                    return false;
                }
                boolean found = !sources
                        .forEachEntry(new TLongObjectProcedure<Timestamp>() {

                            @Override
                            public boolean execute(long field,
                                    Timestamp sourceTs) {
                                Timestamp targetTs = targets.get(field);
                                return targetTs == null
                                        || !sourceTs.before(targetTs);
                            }
                        });
                return found;
            } finally {
                targetSet.close();
            }
        } finally {
            sourceSet.close();
        }
    }

    public boolean happensBeforeVolatile(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        return happensBefore(hbVolWriteSt, hbVolReadSt, write, writeThread,
                read, readThread);
    }

    TLongObjectMap<Timestamp> genSources(ResultSet set) throws SQLException {
        sourcesCache.clear();
        do {
            long targetField = set.getLong(1);
            Timestamp sourceTs = set.getTimestamp(2);
            Timestamp ts = sourcesCache.get(targetField);
            if (ts == null || ts.after(sourceTs)) {
                sourcesCache.put(targetField, sourceTs);
            }
        } while (set.next());
        return sourcesCache;
    }

    TLongObjectMap<Timestamp> genTargets(ResultSet set) throws SQLException {
        targetsCache.clear();
        do {
            long targetField = set.getLong(1);
            Timestamp targetTs = set.getTimestamp(2);
            Timestamp ts = targetsCache.get(targetField);
            if (ts == null || ts.before(targetTs)) {
                targetsCache.put(targetField, targetTs);
            }
        } while (set.next());
        return targetsCache;
    }

    public boolean happensBeforeClassInitialization(Timestamp write,
            long writeThread, Timestamp read, long readThread)
            throws SQLException {
        int idx = 1;
        hbClassInitSt.setLong(idx++, writeThread);
        hbClassInitSt.setTimestamp(idx++, write);
        hbClassInitSt.setLong(idx++, readThread);
        hbClassInitSt.setTimestamp(idx++, read);
        final ResultSet hbSet = hbClassInitSt.executeQuery();
        try {
            return hbSet.next();
        } finally {
            hbSet.close();
        }
    }

    public boolean happensBeforeThread(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        int idx = 1;
        hbSt.setLong(idx++, writeThread);
        hbSt.setLong(idx++, readThread);
        hbSt.setTimestamp(idx++, write);
        hbSt.setTimestamp(idx++, read);
        final ResultSet hbSet = hbSt.executeQuery();
        try {
            return hbSet.next();
        } finally {
            hbSet.close();
        }
    }

    public boolean happensBeforeCollection(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        int idx = 1;
        hbCollSourceSt.setLong(idx++, writeThread);
        hbCollSourceSt.setTimestamp(idx++, write);
        hbCollSourceSt.setTimestamp(idx++, read);
        final ResultSet hbCollSourceSet = hbCollSourceSt.executeQuery();
        try {
            idx = 1;
            hbCollTargetSt.setLong(idx++, readThread);
            hbCollTargetSt.setTimestamp(idx++, write);
            hbCollTargetSt.setTimestamp(idx++, read);
            final ResultSet hbCollTargetSet = hbCollTargetSt.executeQuery();
            try {
                long targetColl = -1;
                long targetObj = -1;
                Timestamp targetTs = null;
                sourceLoop: while (hbCollSourceSet.next()) {
                    long sourceColl = hbCollSourceSet.getLong(1);
                    long sourceObj = hbCollSourceSet.getLong(2);
                    Timestamp sourceTs = hbCollSourceSet.getTimestamp(3);
                    if (sourceObj == targetObj && sourceColl == targetColl) {
                        if (sourceTs.before(targetTs)) {
                            return true;
                        }
                    } else if (sourceObj > targetObj || sourceObj == targetObj
                            && sourceColl > targetColl) {
                        while (hbCollTargetSet.next()) {
                            targetColl = hbCollTargetSet.getLong(1);
                            targetObj = hbCollTargetSet.getLong(2);
                            targetTs = hbCollTargetSet.getTimestamp(3);
                            if (targetObj == sourceObj
                                    && targetColl == sourceColl
                                    && sourceTs.before(targetTs)) {
                                return true;
                            } else if (sourceObj < targetObj
                                    || sourceObj == targetObj
                                    && sourceColl <= targetColl) {
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

    public boolean happensBeforeObject(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        return happensBefore(hbObjSourceSt, hbObjTargetSt, write, writeThread,
                read, readThread);
    }

    public boolean happensBeforeLock(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        return happensBefore(hbLockSourceSt, hbLockTargetSt, write,
                writeThread, read, readThread);
    }

    /**
     * Determines whether or not a happens-before relationship exists between
     * two threads, typically from a write in one thread to a read in another
     * thread. The first timestamp must always be earlier than the second.
     * 
     * @param isStatic
     * 
     * @param write
     * @param writeThread
     * @param read
     * @param readThread
     * @return
     * @throws SQLException
     */
    public boolean hasHappensBefore(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        return write == null
                || writeThread == readThread
                || happensBeforeVolatile(write, writeThread, read, readThread)
                || happensBeforeThread(write, writeThread, read, readThread)
                || happensBeforeObject(write, writeThread, read, readThread)
                || happensBeforeLock(write, writeThread, read, readThread)
                || happensBeforeCollection(write, writeThread, read, readThread)
                || happensBeforeClassInitialization(write, writeThread, read,
                        readThread);
    }

    public List<HBEdge> happensBeforeTraces(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        if (write == null || writeThread == readThread) {
            return Collections.emptyList();
        }
        List<HBEdge> list = new ArrayList<HBEdge>();
        addHappensBeforeVolatile(write, writeThread, read, readThread, list);
        addHappensBeforeThread(write, writeThread, read, readThread, list);
        addHappensBeforeObject(write, writeThread, read, readThread, list);
        addHappensBeforeLock(write, writeThread, read, readThread, list);
        addHappensBeforeCollection(write, writeThread, read, readThread, list);
        addHappensBeforeClassInit(write, writeThread, read, readThread, list);
        return list;
    }

    static class Coll {
        final long coll;
        final long obj;

        public Coll(long coll, long obj) {
            this.coll = coll;
            this.obj = obj;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (coll ^ coll >>> 32);
            result = prime * result + (int) (obj ^ obj >>> 32);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Coll other = (Coll) obj;
            if (coll != other.coll) {
                return false;
            }
            if (this.obj != other.obj) {
                return false;
            }
            return true;
        }

    }

    private void addHappensBeforeCollection(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        int idx = 1;
        hbCollSourceTraceSt.setLong(idx++, writeThread);
        hbCollSourceTraceSt.setTimestamp(idx++, write);
        hbCollSourceTraceSt.setTimestamp(idx++, read);
        final ResultSet hbCollSourceSet = hbCollSourceTraceSt.executeQuery();
        final Map<Coll, HBNode> sources = new HashMap<HappensBeforeAnalysis.Coll, HBNode>();
        final Map<Coll, HBNode> targets = new HashMap<Coll, HBNode>();
        try {
            while (hbCollSourceSet.next()) {
                long sourceColl = hbCollSourceSet.getLong(1);
                long sourceObj = hbCollSourceSet.getLong(2);
                Timestamp sourceTs = hbCollSourceSet.getTimestamp(3);
                long sourceTrace = hbCollSourceSet.getLong(4);
                final Coll source = new Coll(sourceColl, sourceObj);
                final HBNode trace = sources.get(source);
                if (trace == null || trace.ts.after(sourceTs)) {
                    sources.put(source, new HBNode(sourceTs, sourceTrace));
                }
            }
        } finally {
            hbCollSourceSet.close();
        }
        idx = 1;
        hbCollTargetTraceSt.setLong(idx++, readThread);
        hbCollTargetTraceSt.setTimestamp(idx++, write);
        hbCollTargetTraceSt.setTimestamp(idx++, read);
        final ResultSet hbCollTargetSet = hbCollTargetTraceSt.executeQuery();
        try {
            while (hbCollTargetSet.next()) {
                long targetColl = hbCollTargetSet.getLong(1);
                long targetObj = hbCollTargetSet.getLong(2);
                Timestamp targetTs = hbCollTargetSet.getTimestamp(3);
                long targetTrace = hbCollTargetSet.getLong(4);
                final Coll target = new Coll(targetColl, targetObj);
                final HBNode trace = targets.get(target);
                if (trace == null || trace.ts.before(targetTs)) {
                    targets.put(target, new HBNode(targetTs, targetTrace));
                }
            }
        } finally {
            hbCollTargetSet.close();
        }
        for (Entry<Coll, HBNode> sourceEntry : sources.entrySet()) {
            Coll source = sourceEntry.getKey();
            HBNode sourceTrace = sourceEntry.getValue();
            HBNode targetTrace = targets.get(source);
            if (targetTrace != null && targetTrace.ts.after(sourceTrace.ts)) {
                list.add(new HBEdge(sourceTrace, targetTrace));
            }
        }

    }

    private void addHappensBeforeObject(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        addHappensBefore(hbObjSourceTraceSt, hbObjTargetTraceSt, write,
                writeThread, read, readThread, list);
    }

    private void addHappensBeforeLock(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        addHappensBefore(hbLockSourceTraceSt, hbLockTargetTraceSt, write,
                writeThread, read, readThread, list);
    }

    private void addHappensBeforeThread(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        int idx = 1;
        hbTraceSt.setLong(idx++, writeThread);
        hbTraceSt.setLong(idx++, readThread);
        hbTraceSt.setTimestamp(idx++, write);
        hbTraceSt.setTimestamp(idx++, read);
        final ResultSet hbTraceSet = hbTraceSt.executeQuery();
        try {
            while (hbTraceSet.next()) {
                Timestamp ts = hbTraceSet.getTimestamp(1);
                long trace = hbTraceSet.getLong(2);
                HBNode node = new HBNode(ts, trace);
                list.add(new HBEdge(node, null));
            }
        } finally {
            hbTraceSet.close();
        }
    }

    private void addHappensBeforeVolatile(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        addHappensBefore(hbVolWriteTraceSt, hbVolReadTraceSt, write,
                writeThread, read, readThread, list);
    }

    private void addHappensBefore(PreparedStatement sourceSt,
            PreparedStatement targetSt, Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        sourceSt.setLong(1, writeThread);
        sourceSt.setTimestamp(2, write);
        sourceSt.setTimestamp(3, read);
        final ResultSet sourceSet = sourceSt.executeQuery();
        try {
            if (!sourceSet.next()) {
                return;
            }
            targetSt.setLong(1, readThread);
            targetSt.setTimestamp(2, write);
            targetSt.setTimestamp(3, read);
            final ResultSet targetSet = targetSt.executeQuery();
            try {
                if (!targetSet.next()) {
                    return;
                }
                final Map<Long, HBNode> sources = genSourceNodes(sourceSet);
                sourceSet.close();
                if (sources.isEmpty()) {
                    return;
                }
                final Map<Long, HBNode> targets = genTargetNodes(targetSet);
                targetSet.close();
                if (targets.isEmpty()) {
                    return;
                }
                for (Entry<Long, HBNode> source : sources.entrySet()) {
                    long sourceId = source.getKey();
                    HBNode sourceNode = source.getValue();
                    HBNode targetNode = targets.get(sourceId);
                    if (targetNode != null
                            && sourceNode.getTs().before(targetNode.getTs())) {
                        list.add(new HBEdge(sourceNode, targetNode));
                    }
                }
            } finally {
                targetSet.close();
            }
        } finally {
            sourceSet.close();
        }
    }

    private void addHappensBeforeClassInit(Timestamp write, long writeThread,
            Timestamp read, long readThread, List<HBEdge> list)
            throws SQLException {
        int idx = 1;
        hbClassInitTraceSt.setLong(idx++, writeThread);
        hbClassInitTraceSt.setTimestamp(idx++, write);
        hbClassInitTraceSt.setLong(idx++, readThread);
        hbClassInitTraceSt.setTimestamp(idx++, read);
        final ResultSet hbSet = hbClassInitTraceSt.executeQuery();
        try {
            while (hbSet.next()) {
                idx = 1;
                HBNode source = new HBNode(hbSet.getTimestamp(idx++),
                        hbSet.getLong(idx++));
                HBNode target = new HBNode(hbSet.getTimestamp(idx++),
                        hbSet.getLong(idx++));
                list.add(new HBEdge(source, target));
            }
        } finally {
            hbSet.close();
        }
    }

    static class HBNode {
        final Timestamp ts;
        final long trace;

        public HBNode(Timestamp ts, long trace) {
            super();
            this.ts = ts;
            this.trace = trace;
        }

        public Timestamp getTs() {
            return ts;
        }

        public long getTrace() {
            return trace;
        }
    }

    public static class HBEdge {
        final HBNode source;
        final HBNode target;

        public HBNode getSource() {
            return source;
        }

        public HBNode getTarget() {
            return target;
        }

        public HBEdge(HBNode source, HBNode target) {
            super();
            this.source = source;
            this.target = target;
        }

        public Object get(int col) {
            switch (col) {
            case 1:
                return source.getTrace();
            case 2:
                return source.getTs();
            case 3:
                return target == null ? -1 : target.getTrace();
            case 4:
                return target == null ? null : target.getTs();
            default:
                throw new IllegalArgumentException(String.format(
                        "%d is not a valid column.", col));
            }
        }

    }

    Map<Long, HBNode> genSourceNodes(ResultSet set) throws SQLException {
        Map<Long, HBNode> map = new HashMap<Long, HBNode>();
        do {
            long sourceId = set.getLong(1);
            Timestamp sourceTs = set.getTimestamp(2);
            HBNode node = map.get(sourceId);
            if (node == null || node.getTs().after(sourceTs)) {
                map.put(sourceId, new HBNode(sourceTs, set.getLong(3)));
            }
        } while (set.next());
        return map;
    }

    Map<Long, HBNode> genTargetNodes(ResultSet set) throws SQLException {
        Map<Long, HBNode> map = new HashMap<Long, HBNode>();
        do {
            long targetId = set.getLong(1);
            Timestamp targetTs = set.getTimestamp(2);
            HBNode node = map.get(targetId);
            if (node == null || node.getTs().before(targetTs)) {
                map.put(targetId, new HBNode(targetTs, set.getLong(3)));
            }
        } while (set.next());
        return map;
    }

}
