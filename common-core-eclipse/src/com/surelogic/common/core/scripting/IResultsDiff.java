package com.surelogic.common.core.scripting;

import java.io.File;

public interface IResultsDiff {
	boolean isEmpty();
	void write(File diffs);
}
