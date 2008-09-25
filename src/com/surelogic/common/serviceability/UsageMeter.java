package com.surelogic.common.serviceability;

import java.io.File;

import com.surelogic.common.FileUtility;
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

	/**
	 * Gets the file being used to persist this usage meter. If this file needs
	 * to be read than it is important to call {@link #persist()} before reading
	 * its contents.
	 * 
	 * @return the file being used to persist this usage meter.
	 */
	public File getFile() {
		return f_usageFile;
	}

	private XMLMemo f_memo = new XMLMemo(f_usageFile);

	/**
	 * Gets the memo underlying this usage meter. The {@link #tickUse(String)}
	 * method should be used to note the use of a feature rather than direct
	 * interaction with the memo. For registration and copy protection data,
	 * however, direct use of the memo is required.
	 * <p>
	 * To be safe it is recommended that all actions performed on the memo
	 * object be within a block synchronized on this usage meter.
	 * 
	 * <pre>
	 * final UsageMeter m = UsageMeter.getInstance();
	 * synchronized (m) {
	 * 	final XMLMemo memo = m.getMemo();
	 * 	// do stuff with memo
	 * }
	 * </pre>
	 * 
	 * Always use {@link #persist()} rather than calling
	 * {@link XMLMemo#dispose()} on the memo directly.
	 * 
	 * @return the memo underlying this usage meter.
	 */
	public synchronized XMLMemo getMemo() {
		return f_memo;
	}

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
	 * Gets the contents of the file used to persist this usage meter. This
	 * meter is persisted before its contents are read.
	 * <p>
	 * The resulting data is in XML and is ready to be sent to SureLogic.
	 * 
	 * @return the contents of the file used to persist this usage meter.
	 */
	public synchronized String getFileContents() {
		persist();
		return FileUtility.getFileContents(f_usageFile);
	}

	private UsageMeter() {
		// singleton
		f_memo.init();
	}
}
