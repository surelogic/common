package com.surelogic.flashlight.common;

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

    public boolean happensBeforeVolatile(Timestamp write, long writeThread,
            Timestamp read, long readThread) throws SQLException {
        int idx = 1;
        hbVolWriteSt.setLong(idx++, writeThread);
        hbVolWriteSt.setTimestamp(idx++, write);
        hbVolWriteSt.setTimestamp(idx++, read);
        final ResultSet hbVolWriteSet = hbVolWriteSt.executeQuery();
        try {
            idx = 1;
            hbVolReadSt.setLong(idx++, readThread);
            hbVolReadSt.setTimestamp(idx++, write);
            hbVolReadSt.setTimestamp(idx++, read);
            final ResultSet hbVolReadSet = hbVolReadSt.executeQuery();
            try {
                long targetField = -1;
                Timestamp targetTs = null;
                sourceLoop: while (hbVolWriteSet.next()) {
                    long sourceField = hbVolWriteSet.getLong(1);
                    Timestamp sourceTs = hbVolWriteSet.getTimestamp(2);
                    if (sourceField == targetField) {
                        if (sourceTs.before(targetTs)) {
                            return true;
                        }
                    } else if (sourceField > targetField) {
                        while (hbVolReadSet.next()) {
                            targetField = hbVolReadSet.getLong(1);
                            targetTs = hbVolReadSet.getTimestamp(2);
                            if (sourceField == targetField
                                    && sourceTs.before(targetTs)) {
                                isVolatileSt.setLong(1, sourceField);
                                ResultSet isVolatileSet = isVolatileSt
                                        .executeQuery();
                                try {
                                    isVolatileSet.next();
                                    if (isVolatileSet.getString(1).equals("Y")) {
                                        return true;
                                    }
                                } finally {
                                    isVolatileSet.close();
                                }
                            } else if (sourceField <= targetField) {
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
                hbVolReadSet.close();
            }
        } finally {
            hbVolWriteSet.close();
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
        int idx = 1;
        hbObjSourceSt.setLong(idx++, writeThread);
        hbObjSourceSt.setTimestamp(idx++, write);
        hbObjSourceSt.setTimestamp(idx++, read);
        final ResultSet hbObjSourceSet = hbObjSourceSt.executeQuery();
        try {
            idx = 1;
            hbObjTargetSt.setLong(idx++, readThread);
            hbObjTargetSt.setTimestamp(idx++, write);
            hbObjTargetSt.setTimestamp(idx++, read);
            final ResultSet hbObjTargetSet = hbObjTargetSt.executeQuery();
            try {
                long targetObj = -1;
                Timestamp targetTs = null;
                sourceLoop: while (hbObjSourceSet.next()) {
                    long sourceObj = hbObjSourceSet.getLong(1);
                    Timestamp sourceTs = hbObjSourceSet.getTimestamp(2);
                    if (sourceObj == targetObj) {
                        if (sourceTs.before(targetTs)) {
                            return true;
                        }
                    } else if (sourceObj > targetObj) {
                        while (hbObjTargetSet.next()) {
                            targetObj = hbObjTargetSet.getLong(1);
                            targetTs = hbObjTargetSet.getTimestamp(2);
                            if (targetObj == sourceObj
                                    && sourceTs.before(targetTs)) {
                                return true;
                            } else if (sourceObj <= targetObj) {
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
                // || happensBeforeVolatile(write, writeThread, read,
                // readThread)
                || happensBeforeThread(write, writeThread, read, readThread)
                || happensBeforeObject(write, writeThread, read, readThread)
                || happensBeforeCollection(write, writeThread, read, readThread);
    }
}
