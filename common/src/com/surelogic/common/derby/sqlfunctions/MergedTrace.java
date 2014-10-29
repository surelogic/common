package com.surelogic.common.derby.sqlfunctions;

import com.surelogic.flashlight.common.LockType;

public class MergedTrace {
    final long parentId;
    final long id;
    final String clazz;
    final String clazzCode;
    final String pakkage;
    final String file;
    final String loc;
    final String locCode;
    final int line;
    boolean wasNull;

    final boolean isLock;
    final LockType type;
    final long lockId;

    MergedTrace(Trace t) {
        parentId = t.parentId;
        id = t.id;
        clazz = t.clazz;
        clazzCode = t.clazzCode;
        pakkage = t.pakkage;
        file = t.file;
        loc = t.loc;
        locCode = t.locCode;
        line = t.line;
        isLock = false;
        type = null;
        lockId = -1;
    }

    MergedTrace(Trace t, LockType type, long lockId) {
        parentId = t.parentId;
        id = t.id;
        clazz = t.clazz;
        clazzCode = t.clazzCode;
        pakkage = t.pakkage;
        file = t.file;
        loc = t.loc;
        locCode = t.locCode;
        line = t.line;
        isLock = true;
        this.type = type;
        this.lockId = lockId;
    }

    public boolean matches(Trace t) {
        return clazz.equals(t.clazz) && pakkage.equals(t.pakkage)
                && loc.equals(t.loc) && line == t.line;
    }

    public Object wasNull() {
        return wasNull;
    }

    public Object get(int col) {
        wasNull = false;
        switch (col) {
        case 1:
            return lockId;
        case 2:
            return id;
        case 3:
            if (!isLock) {
                wasNull = true;
                return null;
            }
            return type.getFlag();
        case 4:
            return pakkage;
        case 5:
            return clazz;
        case 6:
            return clazzCode;
        case 7:
            return loc;
        case 8:
            return locCode;
        case 9:
            return line;
        case 10:
            return isLock ? "Y" : "N";
        default:
            throw new IllegalArgumentException();
        }
    }
}
