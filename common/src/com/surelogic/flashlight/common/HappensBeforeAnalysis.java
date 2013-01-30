package com.surelogic.flashlight.common;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.surelogic.common.jdbc.QB;

public class HappensBeforeAnalysis {

    private final PreparedStatement hbSt;
    private final PreparedStatement hbVolWriteSt;
    private final PreparedStatement hbVolReadSt;
    private final PreparedStatement hbObjSourceSt;
    private final PreparedStatement hbObjTargetSt;
    private final PreparedStatement hbCollSourceSt;
    private final PreparedStatement hbCollTargetSt;
    private final PreparedStatement isVolatileSt;
    private final PreparedStatement isFinalSt;

    private final TLongObjectMap<Timestamp> targetsCache;
    private final TLongObjectMap<Timestamp> sourcesCache;

    public HappensBeforeAnalysis(Connection conn) throws SQLException {
        hbSt = conn.prepareStatement(QB.get("Accesses.happensBefore"));
        isVolatileSt = conn
                .prepareStatement(QB.get("Accesses.isFieldVolatile"));
        isFinalSt = conn.prepareStatement(QB.get("Accesses.isFieldFinal"));
        hbVolReadSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeVolatileRead"));
        hbVolWriteSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeVolatileWrite"));
        hbObjSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceObject"));
        hbObjTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetObject"));
        hbCollSourceSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeSourceColl"));
        hbCollTargetSt = conn.prepareStatement(QB
                .get("Accesses.happensBeforeTargetColl"));
        targetsCache = new TLongObjectHashMap<Timestamp>();
        sourcesCache = new TLongObjectHashMap<Timestamp>();
    }

    void finished() throws SQLException {
        hbSt.close();
        hbObjSourceSt.close();
        hbObjTargetSt.close();
        hbCollSourceSt.close();
        hbCollTargetSt.close();
        hbVolReadSt.close();
        hbVolWriteSt.close();
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
            targetSt.setLong(1, writeThread);
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
                return !sources
                        .forEachEntry(new TLongObjectProcedure<Timestamp>() {

                            @Override
                            public boolean execute(long field,
                                    Timestamp sourceTs) {
                                Timestamp targetTs = targets.get(field);
                                return targetTs == null
                                        || !sourceTs.before(targetTs);
                            }
                        });
            } finally {
                targetSet.close();
            }
        } finally {
            sourceSet.close();
        }
    }

    public boolean happensBeforeVolatile(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        return happensBefore(hbVolReadSt, hbVolWriteSt, write, writeThread,
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

    /**
     * Determines whether or not a happens-before relationship exists between
     * two threads, typically from a write in one thread to a read in another
     * thread. The first timestamp must always be earlier than the second.
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
                || happensBeforeCollection(write, writeThread, read, readThread);
    }
}
