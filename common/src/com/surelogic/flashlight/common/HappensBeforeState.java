package com.surelogic.flashlight.common;

enum HappensBeforeState {
    FIRST(" "), YES("Yes"), NO("No");
    private final String display;

    HappensBeforeState(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

}