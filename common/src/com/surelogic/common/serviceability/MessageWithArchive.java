package com.surelogic.common.serviceability;

import java.io.File;

public final class MessageWithArchive extends MessageWithLog {
	public MessageWithArchive(File archive) {
		super(archive);
	}
	
	@Override
	public String getMessageTypeString() {
		return "Tip for Improvement";
	}

	@Override
	public String propPfx() {
		return "common.send.archive.wizard.";
	}
}
