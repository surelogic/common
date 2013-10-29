package com.surelogic.flashlight.common;

public enum LockType {
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

    public static LockType fromFlag(String flag) {
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