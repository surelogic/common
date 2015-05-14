package com.surelogic.common;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import com.surelogic.ReferenceObject;
import com.surelogic.common.logging.SLLogger;

/**
 * This memory warning system will call the listener when we exceed the
 * percentage of available memory specified. There should only be one instance
 * of this object created, since the usage threshold can only be set to one
 * number.
 * 
 * http://www.roseindia.net/javatutorials/OutOfMemoryError_Warning_System.shtml
 * 
 * Altered to use collection usage
 */
public enum MemoryWarningSystem {
  INSTANCE;

  final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

  @ReferenceObject
  public interface Listener {
    public void memoryUsageLow(long usedMemory, long maxMemory);
  }

  private MemoryWarningSystem() {
    // TODO is this really the best way of doing this?
    MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
    NotificationEmitter emitter = (NotificationEmitter) mbean;
    emitter.addNotificationListener(new NotificationListener() {
      @Override
      public void handleNotification(Notification n, Object hb) {
        // SLLogger.getLogger().warning("Got notification: "+n.getType());
        if (n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
          long maxMemory = tenuredGenPool.getUsage().getMax();
          long usedMemory = tenuredGenPool.getUsage().getUsed();
          for (Listener listener : listeners) {
            listener.memoryUsageLow(usedMemory, maxMemory);
          }
        }
      }
    }, null, null);
  }

  public boolean addListener(Listener listener) {
    return listeners.add(listener);
  }

  public boolean removeListener(Listener listener) {
    return listeners.remove(listener);
  }

  static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();

  public static void setPercentageUsageThreshold(double percentage) {
    if (percentage <= 0.0 || percentage > 1.0) {
      throw new IllegalArgumentException("Percentage not in range");
    }
    long maxMemory = tenuredGenPool.getUsage().getMax();
    long warningThreshold = (long) (maxMemory * percentage);
    tenuredGenPool.setUsageThreshold(warningThreshold);
  }

  /**
   * Tenured Space Pool can be determined by it being of type HEAP and by it
   * being possible to set the usage threshold.
   */
  private static MemoryPoolMXBean findTenuredGenPool() {
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      // TODO I don't know whether this approach is better, or whether
      // we should rather check for the pool name "Tenured Gen"?
      if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
        return pool;
      }
    }
    throw new AssertionError("Could not find tenured space");
  }

  private static final AtomicBoolean initialized = new AtomicBoolean(false);

  /**
   * Convenience method to setup a default policy
   */
  public static void setDefaultPolicy() {
    if (initialized.getAndSet(true)) {
      return;
    }

    INSTANCE.addListener(new Listener() {
      @Override
      public void memoryUsageLow(long usedMemory, long maxMemory) {
        SLLogger.getLogger().warning("Used " + usedMemory + " out of " + maxMemory);
      }
    });
    setPercentageUsageThreshold(0.90);
  }
}
