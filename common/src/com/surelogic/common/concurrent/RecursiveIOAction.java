package com.surelogic.common.concurrent;

import java.io.IOException;
import java.util.concurrent.RecursiveAction;

public abstract class RecursiveIOAction extends RecursiveAction {
	private static final long serialVersionUID = 1L;

	@Override
	protected final void compute() {
		try {
			compute_private();
		} catch(IOException e) {
			completeExceptionally(e);
		}
	}
	
	protected abstract void compute_private() throws IOException;
}
