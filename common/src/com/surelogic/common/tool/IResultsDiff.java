package com.surelogic.common.tool;

import java.io.File;

public interface IResultsDiff {
	boolean isEmpty();
	void write(File diffs);
}
