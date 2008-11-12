package com.surelogic.common;

import java.io.File;

import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLStatus;

import junit.framework.TestCase;

public class TestFileUtility extends TestCase {

	private final File f_tmpDir = new File(System.getProperty("java.io.tmpdir"));
	private final String f_lf = System.getProperty("line.separator");

	public void testFileReadWrite() {
		final File f = new File(f_tmpDir, "test01.txt");
		if (f.exists())
			f.delete();
		assertFalse(f.exists());

		String text = f.getAbsolutePath();
		FileUtility.putFileContents(f, text);
		String actual = FileUtility.getFileContents(f);
		assertEquals(text, actual);
		assertTrue(f.exists());

		/*
		 * To test line feeds we need to use the "line.separator" property as
		 * the get method uses that for any line feeds.
		 * 
		 * Also, it doesn't remember extra line feeds at the end.
		 */
		text = "foo" + f_lf + f_lf + "\tbar\\";
		FileUtility.putFileContents(f, text);
		actual = FileUtility.getFileContents(f);
		assertEquals(text, actual);
		assertTrue(f.exists());

		assertTrue(FileUtility.recursiveDelete(f));
		assertFalse(f.exists());
	}

	public void testDirectoryOperations() {
		final File f = new File(f_tmpDir, "test01.txt");
		if (f.exists())
			FileUtility.recursiveDelete(f);
		assertTrue(FileUtility.createDirectory(f));
		assertTrue(f.exists());

		for (int i = 0; i < 10; i++) {
			final File c = new File(f, "ooo" + i);
			FileUtility.putFileContents(c, c.getAbsolutePath());
			assertTrue(c.exists());
			final File sd = new File(f, "subdir.dir");
			assertTrue(FileUtility.createDirectory(sd));
			assertTrue(sd.exists());
			assertTrue(sd.isDirectory());
			for (int j = 0; j < 10; j++) {
				final File sdc = new File(sd, "ooo" + i);
				FileUtility.putFileContents(sdc, sdc.getAbsolutePath());
				assertTrue(sdc.exists());
			}
		}

		assertTrue(FileUtility.recursiveDelete(f));
		assertFalse(f.exists());
	}

	public void testCopy() {
		final File f = new File(f_tmpDir, "test01.txt");
		final String text = f.getAbsolutePath();
		FileUtility.putFileContents(f, text);
		assertTrue(f.exists());

		final File d = new File(f_tmpDir, "test02.txt");
		assertTrue(FileUtility.copy(f, d));
		assertTrue(f.exists());
		assertTrue(d.exists());

		assertEquals(text, FileUtility.getFileContents(d));
		assertEquals(FileUtility.getFileContents(f), FileUtility
				.getFileContents(d));

		assertTrue(FileUtility.recursiveDelete(f));
		assertFalse(f.exists());
		assertTrue(FileUtility.recursiveDelete(d));
		assertFalse(d.exists());
	}

	public void testDataDirectory() {
		final File anchor = new File(f_tmpDir, "anchor");
		final File data = new File(f_tmpDir, "data-directory");
		if (anchor.exists())
			FileUtility.recursiveDelete(anchor);
		if (data.exists())
			FileUtility.recursiveDelete(data);

		File result = FileUtility.getDataDirectory(anchor);

		assertEquals(anchor, result);
		assertTrue(FileUtility.recursiveDelete(anchor));

		FileUtility.putFileContents(anchor, data.getAbsolutePath());

		result = FileUtility.getDataDirectory(anchor);

		assertEquals(data, result);

		assertTrue(data.exists());
		assertTrue(data.isDirectory());

		assertTrue(FileUtility.recursiveDelete(anchor));
		assertFalse(anchor.exists());
		assertTrue(FileUtility.recursiveDelete(data));
		assertFalse(data.exists());
	}

	public void testDataDirectoryMove() {
		final File anchor = new File(f_tmpDir, "anchor");
		final File data = new File(f_tmpDir, "data-directory");
		if (anchor.exists())
			FileUtility.recursiveDelete(anchor);
		if (data.exists())
			FileUtility.recursiveDelete(data);

		final String someFile = "dataFile.txt";
		final String someText = "some data text";

		File result = FileUtility.getDataDirectory(anchor);
		assertEquals(anchor, result);
		assertTrue(anchor.exists());
		assertTrue(anchor.isDirectory());

		File some = new File(result, someFile);
		FileUtility.putFileContents(some, someText);
		assertTrue(some.exists());

		SLJob job = FileUtility.moveDataDirectory(anchor, data, true);
		SLStatus status = job.run(new NullSLProgressMonitor());
		assertEquals(status.getMessage(), SLStatus.OK_STATUS, status);
		assertTrue(anchor.exists());
		assertTrue(anchor.isFile());
		assertTrue(data.exists());
		assertTrue(data.isDirectory());

		result = FileUtility.getDataDirectory(anchor);
		assertTrue(result.exists());
		assertTrue(result.isDirectory());
		assertEquals(data, result);

		assertFalse(some.exists());
		some = new File(result, someFile);
		assertTrue(some.exists());
		assertTrue(some.isFile());
		assertEquals(someText, FileUtility.getFileContents(some));

		job = FileUtility.moveDataDirectory(anchor, anchor, true);
		status = job.run(new NullSLProgressMonitor());
		assertEquals(status.getMessage(), SLStatus.OK_STATUS, status);

		result = FileUtility.getDataDirectory(anchor);
		assertEquals(anchor, result);

		assertFalse(some.exists());
		some = new File(result, someFile);
		assertTrue(some.exists());
		assertTrue(some.isFile());
		assertEquals(someText, FileUtility.getFileContents(some));

		assertTrue(FileUtility.recursiveDelete(anchor));
		assertFalse(anchor.exists());
	}
}
