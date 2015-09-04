package com.surelogic.common.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.surelogic.GuardedBy;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Singleton;
import com.surelogic.ThreadSafe;
import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.SLLicenseManager;

/**
 * Used to count things that happen in the tool for feedback to SureLogic.
 * "Things that happened" are identified by a string that is used as a key to
 * the count of how many times that thing occurred.
 */
@ThreadSafe
@Singleton
public final class Counts {

  private static final Counts INSTANCE = new Counts();

  /**
   * Gets the singleton instance.
   * 
   * @return the singleton instance.
   */
  public static Counts getInstance() {
    return INSTANCE;
  };

  @GuardedBy("itself")
  final Map<String, Long> f_counts = new HashMap<>();

  /**
   * Increments the count for the passed key.
   * 
   * @param key
   *          key with which the count is to be incremented.
   * @return the previous value associated with key, or {@code 0} if there was
   *         no prior count for the key.
   * 
   * @throws IllegalArgumentException
   *           if the passed key is {@code null}.
   */
  public long increment(@NonNull final String key) {
    if (key == null)
      throw new IllegalArgumentException(I18N.err(44, "key"));
    final Long result;
    synchronized (f_counts) {
      @Nullable
      final Long count = f_counts.get(key);
      result = f_counts.put(key, Long.valueOf(count == null ? 1L : count + 1L));
    }
    return result == null ? 0L : result.longValue();
  }

  @Override
  public String toString() {
    final ArrayList<String> entries = new ArrayList<>();
    synchronized (f_counts) {
      for (Map.Entry<String, Long> entry : f_counts.entrySet()) {
        entries.add(entry.getKey() + "=" + entry.getValue());
      }
    }
    Collections.sort(entries);
    return Joiner.on(";").skipNulls().join(entries);
  }

  /**
   * Clears the set of counts and parses the passed string to set all key/count
   * values. The passed string should have been created by a call to
   * {@link #toString()} for a {@link Counts} instance.
   * <p>
   * This method is primarily used for persistence.
   * 
   * @param persistedEntries
   *          key/count values; should have been created by a call to
   *          {@link #toString()} for a {@link Counts} instance.
   * 
   * @throws IllegalArgumentException
   *           if persistedEntries is {@code null}.
   */
  public void set(@NonNull final String persistedEntries) {
    if (persistedEntries == null)
      throw new IllegalArgumentException(I18N.err(44, "persistedEntries"));
    synchronized (f_counts) {
      f_counts.clear();
      for (String sEntry : Splitter.on(";").trimResults().omitEmptyStrings().split(persistedEntries)) {
        final int ei = sEntry.indexOf('=');
        if (ei != -1) {
          final String key = sEntry.substring(0, ei);
          final long count = Long.parseLong(sEntry.substring(ei + 1));
          f_counts.put(key, Long.valueOf(count));
        }
      }
    }
  }

  private static final String UNLIKELY_DELIM = "\nSureLogic:Ver:2015-09-04:";

  /**
   * Persists the counts to the end of the license file using an unlikely
   * delimiter.
   */
  public void persist() {
    /*
     * append our information at the end of the file
     */
    FileUtility.appendStringIntoAFile(SLLicenseManager.getInstance().getLicenseFile(),
        UNLIKELY_DELIM + Counts.getInstance().toString());
  }

  /**
   * Reads the license file and tries to get persisted counts, if any, that are
   * written in it.
   */
  public void load() {
    final String contents = FileUtility.getFileContentsAsString(SLLicenseManager.getInstance().getLicenseFile());
    int i = contents.indexOf(UNLIKELY_DELIM);
    if (i != -1) {
      final String value = contents.substring(i + UNLIKELY_DELIM.length());
      if (value.length() > 2)
        Counts.getInstance().set(value);

    }
  }
}
