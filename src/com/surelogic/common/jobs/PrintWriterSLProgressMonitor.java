package com.surelogic.common.jobs;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A progress monitor that prints status reports to a {@link PrintWriter}.
 */
public final class PrintWriterSLProgressMonitor implements SLProgressMonitor {
  public static final class Factory implements SLProgressMonitorFactory {
    private final PrintWriter printWriter;
    
    public Factory(final PrintWriter pw) {
      printWriter = pw;
    }
    
    public SLProgressMonitor createSLProgressMonitor(final String taskName) {
      return new PrintWriterSLProgressMonitor(printWriter, taskName);
    }
  }

  
  
  private static final String INDENT = "\t";
  private static final String BEGIN = " [.";
  private static final String WORK = ".";
  private static final String DONE = "]";
  
  
  
  private final PrintWriter printWriter;
  private final String taskName;
  private int indentLevel = 0;
  private boolean atNewLine = true;
  private final AtomicBoolean isCanceled = new AtomicBoolean(false);

  
  
  public PrintWriterSLProgressMonitor(final PrintWriter pw, final String name) {
    printWriter = pw;
    taskName = name;
  }
  
  
  
  private synchronized void indent() {
    for (int i = 0; i < indentLevel; i++) {
      printWriter.print(INDENT);
    }
  }
  
  
  
  public synchronized void begin() {
    indent();
    printWriter.print(taskName);
    printWriter.print(BEGIN);
    printWriter.flush();
    atNewLine = false;
  }

  public synchronized void begin(int totalWork) {
    indent();
    printWriter.print(taskName);
    printWriter.print(BEGIN);
    printWriter.flush();
    atNewLine = false;
  }

  public synchronized void done() {
    // First close any open subtasks
    while (indentLevel > 0) {
      subTaskDone();
    }
    printWriter.println(DONE);
    printWriter.flush();
    atNewLine = true;
  }

  public synchronized boolean isCanceled() {
    return isCanceled.get();
  }

  public synchronized void setCanceled(final boolean value) {
    isCanceled.set(value);
  }

  public synchronized void subTask(final String name) {
    if (!atNewLine) printWriter.println();
    indentLevel += 1;
    indent();
    printWriter.print(name);
    printWriter.print(BEGIN);
    printWriter.flush();
    atNewLine = false;
  }

  public synchronized void subTaskDone() {
    if (atNewLine) indent();
    printWriter.println(DONE);
    printWriter.flush();
    indentLevel -= 1;
    atNewLine = true;
  }
  
  public synchronized void worked(final int work) {
    printWriter.print(WORK);
    printWriter.flush();
    atNewLine = false;
  }
}
