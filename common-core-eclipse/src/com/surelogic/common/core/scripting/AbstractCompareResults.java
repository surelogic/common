/**
 * Compares the output of JSure to a given oracle file.
 */
package com.surelogic.common.core.scripting;

import java.io.*;

import com.surelogic.common.tool.IResults;
import com.surelogic.common.tool.IResultsDiff;
import com.surelogic.common.tool.IResultsHub;

/**
 * @author ethan
 */
public abstract class AbstractCompareResults extends AbstractCommand {
	private final IResultsHub hub;

	AbstractCompareResults(IResultsHub h) {
		hub = h;
	}
	
	public boolean resultsOk = true;
	
	@Override
	public boolean succeeded() {
		try {
			return resultsOk;
		} finally {
			resultsOk = true;
		}
	}
 	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.test.scripting.ICommand#execute(com.surelogic.test.scripting.ICommandContext,
	 *      java.lang.String[]) The contents array should contain, in the
	 *      following order 
	 *      1 - project name
	 *      2 - results oracle file to compare against 
	 *      3 - the results diffs file (w/ extension)
	 */
	@Override
	public boolean execute(ICommandContext context, String... contents)
			throws Exception {
		resultsOk = true;
		
		//final String projectName = contents[1]; 
		File oracle = resolveFile(context, contents[2]);
		if (oracle == null) {
			return false;
		}
		//PersistentDropInfo.getInstance().load();
		final IResults info = hub.getCurrentResults();
		
		System.out.println("Using oracle: "+oracle);		
		System.out.println("Using current: "+info.getLabel());
		IResultsDiff diff = doDiff(oracle, info);
		
		final File diffs = resolveFile(context, contents[3], true);	
		if (!diff.isEmpty()) {
			if (diffs == null) {
				System.out.println("No file to write diffs to: "+contents[3]);
			} else {
				System.out.println("Writing diffs to "+diffs);	
				diff.write(diffs);
			}
			resultsOk = false;
		} else {
			resultsOk = true;
			
			if (diffs == null) {
				System.out.println("No file, but no diffs to write: "+contents[3]);
			} else {
				System.out.println("No diffs to write ... creating an empty file");			
				diffs.createNewFile();
			}
		}
		/*
    final ITestOutput XML_LOG = IDE.getInstance().makeLog("EclipseLogHandler");
		boolean ok = true;
		System.out
				.println("Try to compare these results to the results oracle");
		assert (new File(oracleName).exists());
		assert (new File(resultsName).exists());

		final Results results = Results.doDiff(oracleName, resultsName);
		if(results == null){
			throw new Exception("Results are null");
		}

		results.generateXML(diffsName);
		System.out.println("diffs.xml = " + diffsName);
		ok = ok && !results.root.hasChildren();
		
		if(!ok){
			throw new Exception("Results " + resultsFile + " don't match the oracle file: " + oracleName);
		}
		*/
		return false;
	}

	protected abstract IResultsDiff doDiff(File oracle, IResults info);
}
