package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.xml.XMLMemo;

/**
 * Tracks use of SureLogic products in a manner that can be reported to
 * SureLogic.
 * <p>
 * This class is thread-safe.
 */
public final class UsageMeter {

	private static UsageMeter INSTANCE = new UsageMeter();

	public static UsageMeter getInstance() {
		return INSTANCE;
	}

	private final File f_usageFile = new File(System.getProperty("user.home")
			+ File.separator + ".surelogic");

	private XMLMemo f_memo;

	/**
	 * Indicates that some named feature was used again. This "ticks" up the
	 * usage on that named feature.
	 * 
	 * @param feature
	 *            a named feature.
	 */
	public synchronized void tickUse(final String feature) {
		if (feature != null && !"".equals(feature)) {
			int useCount = f_memo.getInt(feature, 0);
			useCount++;
			f_memo.setInt(feature, useCount);
		}
	}

	/**
	 * Indicates that this meter should persist any counts. It is fine to call
	 * this method multiple times.
	 */
	public synchronized void persist() {
		f_memo.dispose();
	}

	/**
	 * Clears all usage counters. This likely indicates that the usage data was
	 * sent to SureLogic.
	 */
	public synchronized void reset() {
		f_usageFile.delete();
		f_memo = new XMLMemo(f_usageFile);
		f_memo.init();
	}

	private UsageMeter() {
		// singleton
		f_memo = new XMLMemo(f_usageFile);
		f_memo.init();
	}
}
