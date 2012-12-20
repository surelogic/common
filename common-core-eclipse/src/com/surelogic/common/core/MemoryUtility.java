package com.surelogic.common.core;

import java.io.IOException;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tools.ant.types.CommandlineJava;

import com.surelogic.common.SLUtility;

public class MemoryUtility {

  public static final int MAX_SIZE = 8192;

  private static AtomicInteger f_maxMemorySize = new AtomicInteger();

  /**
   * Computes the maximum heap memory size that can be supported on this
   * platform.
   * 
   * @return the maximum heap size in megabytes.
   */
  public static int computeMaxMemorySizeInMb() {
    final int maxMemorySizeCache = f_maxMemorySize.get();
    if (maxMemorySizeCache > 0)
      return maxMemorySizeCache;

    int phys = computePhysMemorySizeInMb(); // could be -1
    // Try max we want to support
    final int max = Math.min(MAX_SIZE, phys);
    if (runJava(max)) {
      System.out.println("Physical memory " + SLUtility.toStringHumanWithCommas(phys) + " MB, computed "
          + SLUtility.toStringHumanWithCommas(max) + " MB");
      f_maxMemorySize.set(max);
      return max;
    }
    // This doesn't work right on 64-bit JVMs
    phys = SLUtility.getCurrentMaxMemorySizeInMb();
    final int mm = computeMaxMemorySizeInMb(phys, max);
    System.out.println("Physical memory " + SLUtility.toStringHumanWithCommas(phys) + " MB, computed "
        + SLUtility.toStringHumanWithCommas(mm) + " MB");
    f_maxMemorySize.set(mm);
    return mm;
  }

  /**
   * Computes the maximum heap memory size that can be supported on this
   * platform between the passed maximum and minimum values.
   * <p>
   * This method is implemented using a binary search algorithm that tests if
   * half way between {@code minMb} and {@code maxMb} is a possible heap memory
   * size.
   * 
   * @param minMb
   *          A heap size in megabytes is supported by this platform.
   * @param maxMb
   *          A maximum heap size in megabytes to provide an upper bound on
   *          attempts to determine the maximum that can be supported on this
   *          platform.
   * @return the maximum heap size in megabytes, where {@code minMb} <=
   *         <i>result</i> < {@code maxMb}. If {@code maxMb - minMb <=1} then
   *         {@code minMb} is always returned.
   */
  private static int computeMaxMemorySizeInMb(int minMb, int maxMb) {
    final int delta = maxMb - minMb;
    if (delta <= 1) {
      return minMb;
    }
    final int test = minMb + (delta >>> 1);
    if (runJava(test)) {
      return computeMaxMemorySizeInMb(test, maxMb);
    } else {
      return computeMaxMemorySizeInMb(minMb, test);
    }
  }

  /**
   * Runs a trivial Java process to see if it doesn't die because the specified
   * heap size is too large.
   * <p>
   * Consider the example of passing 1024 to this method. It effectively runs
   * <tt>java -Xmx1024m -version</tt> which either works if 1024 megabytes is a
   * possible heap size on this platform or the job fails. The <tt>-version</tt>
   * causes the version to be output and the command to exit immediately in the
   * case that the heap size is possible on this platform.
   * <p>
   * Some code from Apache Ant is used to run the correct JVM.
   * 
   * @param heapSizeMb
   *          the heap size in megabytes to try.
   * @return {@true} if the passed heap size is possible on this platform,
   *         {@code false} if it isn't.
   */
  private static boolean runJava(int heapSizeMb) {
    final CommandlineJava cmdj = new CommandlineJava();
    cmdj.createVmArgument().setValue("-Xmx" + heapSizeMb + 'm');
    cmdj.createVmArgument().setValue("-version");

    final ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());
    try {
      Process p = pb.start();
      return p.waitFor() == 0;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      return false;
    }
  }

  public static int computePhysMemorySizeInMb() {
    OperatingSystemMXBean mxbean = ManagementFactory.getOperatingSystemMXBean();
    try {
      Class<?> c = Class.forName("com.sun.management.OperatingSystemMXBean");
      if (c.isInstance(mxbean)) {
        Method m = c.getDeclaredMethod("getTotalPhysicalMemorySize");
        Long l = (Long) m.invoke(mxbean);
        return (int) (l / (1024 * 1024));
      }
    } catch (Exception e) {
      // ignore it
    }
    return -1;
  }
}
