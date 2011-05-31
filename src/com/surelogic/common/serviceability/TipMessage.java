package com.surelogic.common.serviceability;

public final class TipMessage extends Message {

	@Override
	public String getMessageTypeString() {
		return "Tip for Improvement";
	}

	@Override
	public String propPfx() {
		return "common.send.tip.wizard.";
	}
}
