/**
 * 
 */
package com.surelogic.common.core.scripting;

import java.io.*;

import org.eclipse.core.resources.*;

import com.surelogic.common.FileUtility;
import com.surelogic.common.regression.RegressionUtility;
import com.surelogic.common.tool.IResults;
import com.surelogic.common.tool.IResultsHub;

/**
 * Export the Drop-Sea results as XML to the specified file name
 * 
 * @author ethan
 */
public class ExportResults extends AbstractCommand {
	private final IResultsHub hub;

	ExportResults(IResultsHub h) {
		hub = h;
	}
	
	/**
	 * Arguments are as follows:
	 * 1 - project name
	 * 2 - results file name prefix (w/o suffix)
	 */
	  @Override
	public boolean execute(ICommandContext context, String... contents)
			throws Exception {
		final IProject project = resolveProject(contents[1]);
		final File workspaceFile = project == null ? null : new File(project.getLocationURI()).getParentFile();
		//ws.getRoot().getFullPath().toFile();

		// Export the results from this run
		File location = null;
		String loc = contents[2];
		if (loc.contains("/") || loc.contains("\\")) {
			location = resolveFile(context, loc, true);
		}
		if (location == null) {
			String name;
			if (loc.endsWith(RegressionUtility.JSURE_SNAPSHOT_SUFFIX)) {
				name = loc;
			} else {
				name = loc + RegressionUtility.JSURE_SNAPSHOT_SUFFIX;
			}
			location = new File(workspaceFile, name);
		}
		final IResults info = hub.getCurrentResults();
		final File results = info.getResultsFile();
		boolean success;
		if (!location.getName().endsWith(FileUtility.GZIP_SUFFIX) && results.getName().endsWith(FileUtility.GZIP_SUFFIX)) {
			success = FileUtility.uncompressToCopy(results, location);
		} else {
			success = FileUtility.copy(results, location);
		}
		if (success) {
			System.out.println("Exported: "+location);
			assert (location.exists());
		} else {
			System.out.println("Problem while copying results");
		}
		return false;
	}
}
