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

  final PreparedStatement hbSt;
  final PreparedStatement hbVolWriteSt;
  final PreparedStatement hbVolReadSt;
  final PreparedStatement hbObjSourceSt;
  final PreparedStatement hbObjTargetSt;
  final PreparedStatement hbCollSourceSt;
  final PreparedStatement hbCollTargetSt;
  final PreparedStatement hbLockSourceSt;
  final PreparedStatement hbLockTargetSt;
  final PreparedStatement hbClassInitSt;

  final PreparedStatement hbTraceSt;
  final PreparedStatement hbVolWriteTraceSt;
  final PreparedStatement hbVolReadTraceSt;
  final PreparedStatement hbObjSourceTraceSt;
  final PreparedStatement hbObjTargetTraceSt;
  final PreparedStatement hbLockSourceTraceSt;
  final PreparedStatement hbLockTargetTraceSt;
  final PreparedStatement hbCollSourceTraceSt;
  final PreparedStatement hbCollTargetTraceSt;
  final PreparedStatement hbClassInitTraceSt;
  final PreparedStatement hbLookupAccess;
  final PreparedStatement isFinalSt;
  final PreparedStatement traceMethodCalledSt;
  final TLongObjectMap<Timestamp> targetsCache;
  final TLongObjectMap<Timestamp> sourcesCache;
  final Map<Obj, Timestamp> objTargetsCache;
  final Map<Obj, Timestamp> objSourcesCache;
  final Map<LockId, Timestamp> lockTargetsCache;
  final Map<LockId, Timestamp> lockSourcesCache;

  public HappensBeforeAnalysis(Connection conn) throws SQLException {
    hbSt = conn.prepareStatement(QB.get("Accesses.happensBefore"));
    isFinalSt = conn.prepareStatement(QB.get("Accesses.isFieldFinal"));
    hbVolReadSt = conn.prepareStatement(QB.get("Accesses.happensBeforeVolatileRead"));
    hbVolWriteSt = conn.prepareStatement(QB.get("Accesses.happensBeforeVolatileWrite"));
    hbObjSourceSt = conn.prepareStatement(QB.get("Accesses.happensBeforeSourceObject"));
    hbObjTargetSt = conn.prepareStatement(QB.get("Accesses.happensBeforeTargetObject"));
    hbLockSourceSt = conn.prepareStatement(QB.get("Accesses.happensBeforeSourceLock"));
    hbLockTargetSt = conn.prepareStatement(QB.get("Accesses.happensBeforeTargetLock"));
    hbCollSourceSt = conn.prepareStatement(QB.get("Accesses.happensBeforeSourceColl"));
    hbCollTargetSt = conn.prepareStatement(QB.get("Accesses.happensBeforeTargetColl"));
    hbClassInitSt = conn.prepareStatement(QB.get("Accesses.happensBeforeClassInit"));
    hbTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBefore"));
    hbVolReadTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeVolatileRead"));
    hbVolWriteTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeVolatileWrite"));
    hbObjSourceTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeSourceObject"));
    hbObjTargetTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeTargetObject"));
    hbCollSourceTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeSourceColl"));
    hbCollTargetTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeTargetColl"));
    hbLockSourceTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeSourceLock"));
    hbLockTargetTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeTargetLock"));
    hbClassInitTraceSt = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeClassInit"));
    hbLookupAccess = conn.prepareStatement(QB.get("Accesses.trace.happensBeforeLookupAccess"));
    traceMethodCalledSt = conn.prepareStatement(QB.get("Accesses.trace.traceMethodCalled"));
    targetsCache = new TLongObjectHashMap<>();
    sourcesCache = new TLongObjectHashMap<>();
    objSourcesCache = new HashMap<>();
    objTargetsCache = new HashMap<>();
    lockTargetsCache = new HashMap<>();
    lockSourcesCache = new HashMap<>();
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

  public boolean happensBefore(PreparedStatement sourceSt, PreparedStatement targetSt, Timestamp write, long writeThread,
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
        boolean found = !sources.forEachEntry(new TLongObjectProcedure<Timestamp>() {

          @Override
          public boolean execute(long field, Timestamp sourceTs) {
            Timestamp targetTs = targets.get(field);
            return targetTs == null || !sourceTs.before(targetTs);
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

  public boolean happensBeforeVolatile(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    return happensBefore(hbVolWriteSt, hbVolReadSt, write, writeThread, read, readThread);
  }

  Map<LockId, Timestamp> genLockSources(ResultSet set) throws SQLException {
    lockSourcesCache.clear();
    do {
      LockId sourceLock = new LockId(set.getLong(1), LockType.fromFlag(set.getString(2)));
      Timestamp sourceTs = set.getTimestamp(3);
      Timestamp ts = lockSourcesCache.get(sourceLock);
      if (ts == null || ts.after(sourceTs)) {
        lockSourcesCache.put(sourceLock, sourceTs);
      }
    } while (set.next());
    return lockSourcesCache;
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

  Map<LockId, Timestamp> genLockTargets(ResultSet set) throws SQLException {
    lockTargetsCache.clear();
    do {
      LockId targetLock = new LockId(set.getLong(1), LockType.fromFlag(set.getString(2)));
      Timestamp targetTs = set.getTimestamp(3);
      Timestamp ts = lockTargetsCache.get(targetLock);
      if (ts == null || ts.before(targetTs)) {
        lockTargetsCache.put(targetLock, targetTs);
      }
    } while (set.next());
    return lockTargetsCache;
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

  public boolean happensBeforeClassInitialization(Timestamp write, long writeThread, Timestamp read, long readThread)
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

  private static class Possible {
    final Timestamp ts;
    final long target;

    Possible(long target, Timestamp ts) {
      this.target = target;
      this.ts = ts;
    }
  }

  public boolean happensBeforeThread(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    int idx = 1;
    hbSt.setLong(idx++, writeThread);
    hbSt.setTimestamp(idx++, write);
    hbSt.setTimestamp(idx++, read);
    final ResultSet hbSet = hbSt.executeQuery();
    final List<Possible> possibles = new ArrayList<>();
    try {
      while (hbSet.next()) {
        long target = hbSet.getLong(1);
        Timestamp ts = hbSet.getTimestamp(2);
        if (target == readThread) {
          return true;
        }
        possibles.add(new Possible(target, ts));
      }
    } finally {
      hbSet.close();
    }
    for (Possible p : possibles) {
      if (happensBeforeThread(p.ts, p.target, read, readThread)) {
        return true;
      }
    }
    return false;
  }

  public boolean happensBeforeCollection(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
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
        String targetId = null;
        sourceLoop: while (hbCollSourceSet.next()) {
          long sourceColl = hbCollSourceSet.getLong(1);
          long sourceObj = hbCollSourceSet.getLong(2);
          Timestamp sourceTs = hbCollSourceSet.getTimestamp(3);
          String sourceId = hbCollSourceSet.getString(4);
          if (sourceObj == targetObj && sourceColl == targetColl && sourceId.equals(targetId)) {
            if (sourceTs.before(targetTs)) {
              return true;
            }
          } else if (sourceObj > targetObj || sourceObj == targetObj && sourceColl > targetColl || sourceObj == targetObj
              && sourceColl == targetColl && sourceId.compareTo(targetId) > 0) {
            while (hbCollTargetSet.next()) {
              targetColl = hbCollTargetSet.getLong(1);
              targetObj = hbCollTargetSet.getLong(2);
              targetTs = hbCollTargetSet.getTimestamp(3);
              targetId = hbCollTargetSet.getString(4);
              if (targetObj == sourceObj && targetColl == sourceColl && targetId.equals(sourceId) && sourceTs.before(targetTs)) {
                return true;
              } else if (sourceObj < targetObj || sourceObj == targetObj && sourceColl <= targetColl || sourceObj == targetObj
                  && sourceColl == targetColl && sourceId.compareTo(targetId) <= 0) {
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

  public boolean happensBeforeObject(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    PreparedStatement sourceSt = hbObjSourceSt;
    PreparedStatement targetSt = hbObjTargetSt;
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
        final Map<Obj, Timestamp> sources = genObjSources(sourceSet);
        sourceSet.close();
        if (sources.isEmpty()) {
          return false;
        }
        final Map<Obj, Timestamp> targets = genObjTargets(targetSet);
        targetSet.close();
        if (targets.isEmpty()) {
          return false;
        }
        for (Entry<Obj, Timestamp> sourceEntry : sources.entrySet()) {
          Timestamp targetTs = targets.get(sourceEntry.getKey());
          if (targetTs != null && sourceEntry.getValue().before(targetTs)) {
            return true;
          }
        }
      } finally {
        targetSet.close();
      }
    } finally {
      sourceSet.close();
    }
    return false;
  }

  private Map<Obj, Timestamp> genObjTargets(ResultSet set) throws SQLException {
    objTargetsCache.clear();
    do {
      long o = set.getLong(1);
      Timestamp targetTs = set.getTimestamp(2);
      String id = set.getString(3);
      Obj targetObj = new Obj(o, id);
      Timestamp ts = objTargetsCache.get(targetObj);
      if (ts == null || ts.before(targetTs)) {
        objTargetsCache.put(targetObj, targetTs);
      }
    } while (set.next());
    return objTargetsCache;
  }

  private Map<Obj, Timestamp> genObjSources(ResultSet set) throws SQLException {
    objSourcesCache.clear();
    do {
      long o = set.getLong(1);
      Timestamp sourceTs = set.getTimestamp(2);
      String id = set.getString(3);
      Obj sourceObj = new Obj(o, id);
      Timestamp ts = objSourcesCache.get(sourceObj);
      if (ts == null || ts.after(sourceTs)) {
        objSourcesCache.put(sourceObj, sourceTs);
      }
    } while (set.next());
    return objSourcesCache;
  }

  public boolean happensBeforeLock(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    hbLockSourceSt.setLong(1, writeThread);
    hbLockSourceSt.setTimestamp(2, write);
    hbLockSourceSt.setTimestamp(3, read);
    final ResultSet sourceSet = hbLockSourceSt.executeQuery();
    try {
      if (!sourceSet.next()) {
        return false;
      }
      hbLockTargetSt.setLong(1, readThread);
      hbLockTargetSt.setTimestamp(2, write);
      hbLockTargetSt.setTimestamp(3, read);
      final ResultSet targetSet = hbLockTargetSt.executeQuery();
      try {
        if (!targetSet.next()) {
          return false;
        }
        final Map<LockId, Timestamp> sources = genLockSources(sourceSet);
        sourceSet.close();
        if (sources.isEmpty()) {
          return false;
        }
        final Map<LockId, Timestamp> targets = genLockTargets(targetSet);
        targetSet.close();
        if (targets.isEmpty()) {
          return false;
        }
        for (Entry<LockId, Timestamp> sourceEntry : sources.entrySet()) {
          Timestamp targetTs = targets.get(sourceEntry.getKey());
          if (targetTs != null && sourceEntry.getValue().before(targetTs)) {
            return true;
          }
        }
      } finally {
        targetSet.close();
      }
    } finally {
      sourceSet.close();
    }
    return false;
  }

  /**
   * Determines whether or not a happens-before relationship exists between two
   * threads, typically from a write in one thread to a read in another thread.
   * The first timestamp must always be earlier than the second.
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
  public boolean hasHappensBefore(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    return write == null || writeThread == readThread || happensBeforeVolatile(write, writeThread, read, readThread)
        || happensBeforeThread(write, writeThread, read, readThread) || happensBeforeObject(write, writeThread, read, readThread)
        || happensBeforeLock(write, writeThread, read, readThread) || happensBeforeCollection(write, writeThread, read, readThread)
        || happensBeforeClassInitialization(write, writeThread, read, readThread);
  }

  public List<HBEdge> happensBeforeTraces(Timestamp write, long writeThread, Timestamp read, long readThread) throws SQLException {
    if (write == null) {
      return Collections.emptyList();
    }
    List<HBEdge> list = new ArrayList<>();
    if (readThread == writeThread) {
      hbLookupAccess.setTimestamp(1, write);
      hbLookupAccess.setLong(2, writeThread);
      ResultSet set = hbLookupAccess.executeQuery();
      try {
        if (set.next()) {
          list.add(new HBEdge(new HBNode(write, set.getLong(1)), null, EdgeType.WRITE_IN_THREAD, readThread, writeThread));
        }
      } finally {
        set.close();
      }

    } else {
      addHappensBeforeVolatile(write, writeThread, read, readThread, list);
      addHappensBeforeThread(write, writeThread, read, readThread, list);
      addHappensBeforeObject(write, writeThread, read, readThread, list);
      addHappensBeforeLock(write, writeThread, read, readThread, list);
      addHappensBeforeCollection(write, writeThread, read, readThread, list);
      addHappensBeforeClassInit(write, writeThread, read, readThread, list);
    }
    return list;
  }

  static class Obj {
    final long obj;
    final String id;

    public Obj(long obj, String id) {
      super();
      this.obj = obj;
      this.id = id;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (id == null ? 0 : id.hashCode());
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
      Obj other = (Obj) obj;
      if (id == null) {
        if (other.id != null) {
          return false;
        }
      } else if (!id.equals(other.id)) {
        return false;
      }
      if (this.obj != other.obj) {
        return false;
      }
      return true;
    }

  }

  static class Coll {
    final long coll;
    final long obj;
    final String id;

    public Coll(long coll, long obj, String id) {
      super();
      this.coll = coll;
      this.obj = obj;
      this.id = id;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (coll ^ coll >>> 32);
      result = prime * result + (id == null ? 0 : id.hashCode());
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
      if (id == null) {
        if (other.id != null) {
          return false;
        }
      } else if (!id.equals(other.id)) {
        return false;
      }
      if (this.obj != other.obj) {
        return false;
      }
      return true;
    }

  }

  private void addHappensBeforeCollection(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
      throws SQLException {
    int idx = 1;
    hbCollSourceTraceSt.setLong(idx++, writeThread);
    hbCollSourceTraceSt.setTimestamp(idx++, write);
    hbCollSourceTraceSt.setTimestamp(idx++, read);
    final ResultSet hbCollSourceSet = hbCollSourceTraceSt.executeQuery();
    final Map<Coll, HBNode> sources = new HashMap<>();
    final Map<Coll, HBNode> targets = new HashMap<>();
    try {
      while (hbCollSourceSet.next()) {
        long sourceColl = hbCollSourceSet.getLong(1);
        long sourceObj = hbCollSourceSet.getLong(2);
        Timestamp sourceTs = hbCollSourceSet.getTimestamp(3);
        long sourceTrace = hbCollSourceSet.getLong(4);
        String id = hbCollSourceSet.getString(5);
        final Coll source = new Coll(sourceColl, sourceObj, id);
        final HBNode trace = sources.get(source);
        if (trace == null || trace.ts.after(sourceTs)) {
          sources.put(source, new HBNode(id, sourceTs, sourceTrace));
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
        String id = hbCollTargetSet.getString(5);
        final Coll target = new Coll(targetColl, targetObj, id);
        final HBNode trace = targets.get(target);
        if (trace == null || trace.ts.before(targetTs)) {
          targets.put(target, new HBNode(id, targetTs, targetTrace));
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
        list.add(new HBEdge(sourceTrace, targetTrace, EdgeType.COLLECTION, writeThread, readThread));
      }
    }

  }

  private void addHappensBeforeObject(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
      throws SQLException {
    addHappensBefore(hbObjSourceTraceSt, hbObjTargetTraceSt, write, writeThread, read, readThread, list, EdgeType.OBJECT);
    hbObjSourceTraceSt.setLong(1, writeThread);
    hbObjSourceTraceSt.setTimestamp(2, write);
    hbObjSourceTraceSt.setTimestamp(3, read);
    final ResultSet sourceSet = hbObjSourceTraceSt.executeQuery();
    try {
      if (!sourceSet.next()) {
        return;
      }
      hbObjTargetTraceSt.setLong(1, readThread);
      hbObjTargetTraceSt.setTimestamp(2, write);
      hbObjTargetTraceSt.setTimestamp(3, read);
      final ResultSet targetSet = hbObjTargetTraceSt.executeQuery();
      try {
        if (!targetSet.next()) {
          return;
        }
        final Map<Obj, HBNode> sources = genSourceObjNodes(sourceSet);
        sourceSet.close();
        if (sources.isEmpty()) {
          return;
        }
        final Map<Obj, HBNode> targets = genTargetObjNodes(targetSet);
        targetSet.close();
        if (targets.isEmpty()) {
          return;
        }
        for (Entry<Obj, HBNode> source : sources.entrySet()) {
          Obj sourceId = source.getKey();
          HBNode sourceNode = source.getValue();
          HBNode targetNode = targets.get(sourceId);
          if (targetNode != null && sourceNode.getTs().before(targetNode.getTs())) {
            list.add(new HBEdge(sourceNode, targetNode, EdgeType.OBJECT, writeThread, readThread));
          }
        }
      } finally {
        targetSet.close();
      }
    } finally {
      sourceSet.close();
    }
  }

  private Map<Obj, HBNode> genTargetObjNodes(ResultSet set) throws SQLException {
    Map<Obj, HBNode> map = new HashMap<>();
    do {
      long o = set.getLong(1);
      Timestamp targetTs = set.getTimestamp(2);
      long trace = set.getLong(3);
      String id = set.getString(4);
      Obj targetId = new Obj(o, id);
      HBNode node = map.get(targetId);
      if (node == null || node.getTs().before(targetTs)) {
        map.put(targetId, new HBNode(id, targetTs, trace));
      }
    } while (set.next());
    return map;
  }

  private Map<Obj, HBNode> genSourceObjNodes(ResultSet set) throws SQLException {
    Map<Obj, HBNode> map = new HashMap<>();
    do {
      long o = set.getLong(1);
      Timestamp sourceTs = set.getTimestamp(2);
      long trace = set.getLong(3);
      String id = set.getString(4);
      Obj sourceId = new Obj(o, id);
      HBNode node = map.get(sourceId);
      if (node == null || node.getTs().after(sourceTs)) {
        map.put(sourceId, new HBNode(id, sourceTs, trace));
      }
    } while (set.next());
    return map;
  }

  private void addHappensBeforeLock(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
      throws SQLException {
    hbLockSourceTraceSt.setLong(1, writeThread);
    hbLockSourceTraceSt.setTimestamp(2, write);
    hbLockSourceTraceSt.setTimestamp(3, read);
    final ResultSet sourceSet = hbLockSourceTraceSt.executeQuery();
    try {
      if (!sourceSet.next()) {
        return;
      }
      hbLockTargetTraceSt.setLong(1, readThread);
      hbLockTargetTraceSt.setTimestamp(2, write);
      hbLockTargetTraceSt.setTimestamp(3, read);
      final ResultSet targetSet = hbLockTargetTraceSt.executeQuery();
      try {
        if (!targetSet.next()) {
          return;
        }
        final Map<LockId, HBNode> sources = genSourceLockNodes(sourceSet);
        sourceSet.close();
        if (sources.isEmpty()) {
          return;
        }
        final Map<LockId, HBNode> targets = genTargetLockNodes(targetSet);
        targetSet.close();
        if (targets.isEmpty()) {
          return;
        }
        for (Entry<LockId, HBNode> source : sources.entrySet()) {
          LockId sourceId = source.getKey();
          HBNode sourceNode = source.getValue();
          HBNode targetNode = targets.get(sourceId);
          if (targetNode != null && sourceNode.getTs().before(targetNode.getTs())) {
            list.add(new HBEdge(sourceNode, targetNode, EdgeType.LOCK, writeThread, readThread));
          }
        }
      } finally {
        targetSet.close();
      }
    } finally {
      sourceSet.close();
    }
  }

  static class ToCheck {
    final long thread;
    final HBNode node;

    ToCheck(HBNode node, long thread) {
      this.node = node;
      this.thread = thread;
    }
  }

  private boolean addHappensBeforeThread(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
      throws SQLException {
    int idx = 1;
    hbTraceSt.setLong(idx++, writeThread);
    hbTraceSt.setTimestamp(idx++, write);
    hbTraceSt.setTimestamp(idx++, read);
    final ResultSet hbTraceSet = hbTraceSt.executeQuery();
    final List<ToCheck> toCheck = new ArrayList<>();
    try {
      while (hbTraceSet.next()) {
        Timestamp ts = hbTraceSet.getTimestamp(1);
        long trace = hbTraceSet.getLong(2);
        String id = hbTraceSet.getString(3);
        long targetThread = hbTraceSet.getLong(4);
        HBNode node = new HBNode(id, ts, trace);
        toCheck.add(new ToCheck(node, targetThread));
      }
    } finally {
      hbTraceSet.close();
    }
    boolean added = false;
    for (ToCheck tc : toCheck) {
      if (tc.thread == readThread || addHappensBeforeThread(tc.node.ts, tc.thread, read, readThread, list)) {
        list.add(new HBEdge(tc.node, null, EdgeType.THREAD, writeThread, tc.thread));
        added = true;
      }
    }
    return added;
  }

  private void addHappensBeforeVolatile(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
      throws SQLException {
    addHappensBefore(hbVolWriteTraceSt, hbVolReadTraceSt, write, writeThread, read, readThread, list, EdgeType.VOLATILE);
  }

  private void addHappensBefore(PreparedStatement sourceSt, PreparedStatement targetSt, Timestamp write, long writeThread,
      Timestamp read, long readThread, List<HBEdge> list, EdgeType edgeType) throws SQLException {
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
          if (targetNode != null && sourceNode.getTs().before(targetNode.getTs())) {
            list.add(new HBEdge(sourceNode, targetNode, edgeType, writeThread, readThread));
          }
        }
      } finally {
        targetSet.close();
      }
    } finally {
      sourceSet.close();
    }
  }

  private void addHappensBeforeClassInit(Timestamp write, long writeThread, Timestamp read, long readThread, List<HBEdge> list)
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
        HBNode source = new HBNode(hbSet.getTimestamp(idx++), hbSet.getLong(idx++));
        HBNode target = new HBNode(hbSet.getTimestamp(idx++), hbSet.getLong(idx++));
        list.add(new HBEdge(className(hbSet.getString(idx++), hbSet.getString(idx++)), source, target,
            EdgeType.CLASS_INITIALIZATION, writeThread, readThread));
      }
    } finally {
      hbSet.close();
    }
  }

  private static String className(String pakkage, String clazz) {
    return pakkage == null || pakkage.isEmpty() ? clazz : pakkage + "." + clazz;
  }

  static class HBNode {
    final Timestamp ts;
    final long trace;
    final String id;

    public HBNode(String id, Timestamp ts, long trace) {
      this.ts = ts;
      this.trace = trace;
      this.id = id;
    }

    public HBNode(Timestamp ts, long trace) {
      this.ts = ts;
      this.trace = trace;
      id = null;
    }

    public String getId() {
      return id;
    }

    public Timestamp getTs() {
      return ts;
    }

    public long getTrace() {
      return trace;
    }

  }

  enum EdgeType {
    VOLATILE("Volatile write/read of %s"), WRITE_IN_THREAD("The previous write was in this thread"), OBJECT("%s"), THREAD("%s"), COLLECTION(
        "%s"), CLASS_INITIALIZATION("Class initialization of %s"), LOCK("%s lock release to lock acquisition");

    private final String desc;

    EdgeType(String desc) {
      this.desc = desc;
    }

    String displayId(String id) {
      return String.format(desc, id);
    }
  }

  public class HBEdge {
    final HBNode source;
    final HBNode target;
    final EdgeType type;
    final String id;
    final String sourceMethod;
    final String targetMethod;
    final long sourceThread;
    final long targetThread;

    public EdgeType getType() {
      return type;
    }

    public HBNode getSource() {
      return source;
    }

    public HBNode getTarget() {
      return target;
    }

    HBEdge(HBNode source, HBNode target, EdgeType type, long sourceThread, long targetThread) throws SQLException {
      id = type.displayId(source.getId());
      this.source = source;
      this.target = target;
      this.type = type;
      targetMethod = getTargetMethod();
      sourceMethod = getSourceMethod();
      this.sourceThread = sourceThread;
      this.targetThread = targetThread;
    }

    HBEdge(String id, HBNode source, HBNode target, EdgeType type, long sourceThread, long targetThread) throws SQLException {
      this.id = type.displayId(id);
      this.source = source;
      this.target = target;
      this.type = type;
      targetMethod = getTargetMethod();
      sourceMethod = getSourceMethod();
      this.sourceThread = sourceThread;
      this.targetThread = targetThread;
    }

    String getTargetMethod() throws SQLException {
      switch (getType()) {
      case VOLATILE:
        break;
      case WRITE_IN_THREAD:
        break;
      case CLASS_INITIALIZATION:
        break;
      case COLLECTION:
      case LOCK:
      case OBJECT:
      case THREAD:
        if (getTarget() != null) {
          traceMethodCalledSt.setLong(1, getTarget().getTrace());
          ResultSet set = traceMethodCalledSt.executeQuery();
          try {
            if (set.next()) {
              String clazz = set.getString(1);
              String method = set.getString(2);
              if (clazz != null) {
                return clazz.replaceAll("/", ".") + '.' + method;
              }
            }
          } finally {
            set.close();
          }
        }
      }
      return null;
    }

    String getSourceMethod() throws SQLException {
      switch (getType()) {
      case VOLATILE:
        break;
      case WRITE_IN_THREAD:
        break;
      case CLASS_INITIALIZATION:
        break;
      case COLLECTION:
      case LOCK:
      case OBJECT:
      case THREAD:
        traceMethodCalledSt.setLong(1, getSource().getTrace());
        ResultSet set = traceMethodCalledSt.executeQuery();
        try {
          if (set.next()) {
            String clazz = set.getString(1);
            String method = set.getString(2);
            if (clazz != null) {
              return clazz.replaceAll("/", ".") + '.' + method;
            }
          }
        } finally {
          set.close();
        }
      }
      return null;
    }

    private boolean wasNull;

    public boolean wasNull() {
      return wasNull;
    }

    public Object get(int col) {
      switch (col) {
      case 1:
        wasNull = false;
        return source.getTrace();
      case 2:
        wasNull = false;
        return source.getTs();
      case 3:
        wasNull = target == null;
        return target == null ? 0 : target.getTrace();
      case 4:
        wasNull = target == null;
        return target == null ? null : target.getTs();
      case 5:
        return sourceThread;
      case 6:
        return targetThread;
      case 7:
        wasNull = sourceMethod == null;
        return sourceMethod;
      case 8:
        wasNull = targetMethod == null;
        return targetMethod;
      case 9:
        wasNull = id == null;
        return id;
      default:
        throw new IllegalArgumentException(String.format("%d is not a valid column.", col));
      }
    }
  }

  Map<Long, HBNode> genSourceNodes(ResultSet set) throws SQLException {
    Map<Long, HBNode> map = new HashMap<>();
    do {
      long sourceId = set.getLong(1);
      Timestamp sourceTs = set.getTimestamp(2);
      HBNode node = map.get(sourceId);
      if (node == null || node.getTs().after(sourceTs)) {
        long trace = set.getLong(3);
        String id = set.getString(4);
        map.put(sourceId, new HBNode(id, sourceTs, trace));
      }
    } while (set.next());
    return map;
  }

  Map<LockId, HBNode> genSourceLockNodes(ResultSet set) throws SQLException {
    Map<LockId, HBNode> map = new HashMap<>();
    do {
      LockId sourceId = new LockId(set.getLong(1), LockType.fromFlag(set.getString(2)));
      Timestamp sourceTs = set.getTimestamp(3);
      HBNode node = map.get(sourceId);
      if (node == null || node.getTs().after(sourceTs)) {
        long trace = set.getLong(4);
        String id = set.getString(5);
        map.put(sourceId, new HBNode(id, sourceTs, trace));
      }
    } while (set.next());
    return map;
  }

  Map<LockId, HBNode> genTargetLockNodes(ResultSet set) throws SQLException {
    Map<LockId, HBNode> map = new HashMap<>();
    do {
      LockId targetId = new LockId(set.getLong(1), LockType.fromFlag(set.getString(2)));
      Timestamp targetTs = set.getTimestamp(3);
      HBNode node = map.get(targetId);
      if (node == null || node.getTs().before(targetTs)) {
        long trace = set.getLong(4);
        String id = set.getString(5);
        map.put(targetId, new HBNode(id, targetTs, trace));
      }
    } while (set.next());
    return map;
  }

  Map<Long, HBNode> genTargetNodes(ResultSet set) throws SQLException {
    Map<Long, HBNode> map = new HashMap<>();
    do {
      long targetId = set.getLong(1);
      Timestamp targetTs = set.getTimestamp(2);
      HBNode node = map.get(targetId);
      if (node == null || node.getTs().before(targetTs)) {
        long trace = set.getLong(3);
        String id = set.getString(4);
        map.put(targetId, new HBNode(id, targetTs, trace));
      }
    } while (set.next());
    return map;
  }

}
