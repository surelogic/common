package com.surelogic.common.feedback;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.surelogic.GuardedBy;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Singleton;
import com.surelogic.ThreadSafe;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.license.SLLicenseManager;
import com.surelogic.common.logging.SLLogger;

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
   * Gets a copy of counts for this object.
   * 
   * @return a copy of counts for this object.
   */
  public Map<String, Long> getCounts() {
    synchronized (f_counts) {
      return new HashMap<>(f_counts);
    }
  }

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
    parseHelper(persistedEntries, true);
  }

  /**
   * Parses the passed string to add all key/count values to this instance. The
   * passed string should have been created by a call to {@link #toString()} for
   * a {@link Counts} instance.
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
  public void add(@NonNull final String persistedEntries) {
    if (persistedEntries == null)
      throw new IllegalArgumentException(I18N.err(44, "persistedEntries"));
    parseHelper(persistedEntries, false);
  }

  /**
   * Parses the passed persisted entries and adds them to this counts instance.
   * Optionally the counts may be cleared.
   * 
   * @param persistedEntries
   *          key/count values; should have been created by a call to
   *          {@link #toString()} for a {@link Counts} instance.
   * @param clear
   *          {@code true} if counts for this instance should be cleared before
   *          parsing the passed persisted entries, {@code false} if the
   *          existing counts should be added to.
   */
  private void parseHelper(@NonNull final String persistedEntries, final boolean clear) {
    synchronized (f_counts) {
      if (clear)
        f_counts.clear();
      for (String sEntry : Splitter.on(";").trimResults().omitEmptyStrings().split(persistedEntries)) {
        final int ei = sEntry.indexOf('=');
        if (ei != -1) {
          final String key = sEntry.substring(0, ei);
          long count = Long.parseLong(sEntry.substring(ei + 1));
          Long value = f_counts.get(key);
          if (value != null)
            count += value.longValue();
          f_counts.put(key, Long.valueOf(count));
        }
      }
    }
  }

  /*
   * This is a bit messy but we add the counts information to the end of the
   * license file. It base64 encodes the counts.
   */

  private static final String UNLIKELY_DELIM = "SureLogic:Ver:2015-09-04:";

  /**
   * Persists the counts to the end of the license file using an unlikely
   * delimiter.
   */
  public void persist() {
    final String info = Counts.getInstance().toString();
    final String infoForFile = SLUtility.encodeBase64(info);
    try {
      boolean replaced = false;
      final List<String> lines = Files.readLines(SLLicenseManager.getInstance().getLicenseFile(), Charset.defaultCharset());
      for (ListIterator<String> iterator = lines.listIterator(); iterator.hasNext();) {
        final String line = iterator.next();
        if (line.startsWith(UNLIKELY_DELIM)) {
          // replace counts in file
          iterator.set(UNLIKELY_DELIM + infoForFile);
          replaced = true;
          break;
        }
      }
      if (!replaced) // no counts existed
        lines.add(UNLIKELY_DELIM + infoForFile);
      Files.asCharSink(SLLicenseManager.getInstance().getLicenseFile(), Charset.defaultCharset()).writeLines(lines);

    } catch (IOException e) {
      SLLogger.getLogger().log(Level.WARNING,
          I18N.err(367, "persist", SLLicenseManager.getInstance().getLicenseFile().getAbsolutePath()), e);
    }
  }

  /**
   * Reads the license file and tries to get persisted counts, if any, that are
   * written in it.
   */
  public void load() {
    try {
      final List<String> lines = Files.readLines(SLLicenseManager.getInstance().getLicenseFile(), Charset.defaultCharset());
      for (String line : lines) {
        if (line.startsWith(UNLIKELY_DELIM)) {
          final String infoFromFile = line.substring(UNLIKELY_DELIM.length());
          if (infoFromFile.length() > 2) {
            try {
              final String info = SLUtility.decodeBase64(infoFromFile);
              Counts.getInstance().set(info);
            } catch (Exception decodeFailure) {
              SLLogger.getLogger().log(Level.WARNING,
                  I18N.err(368, infoFromFile, SLLicenseManager.getInstance().getLicenseFile().getAbsolutePath()), decodeFailure);
            }
          }
        }
      }
    } catch (IOException e) {
      SLLogger.getLogger().log(Level.WARNING,
          I18N.err(367, "load", SLLicenseManager.getInstance().getLicenseFile().getAbsolutePath()), e);
    }
  }

  /**
   * Removes all counts. Typically done after they are successfully sent to
   * SureLogic.
   */
  public void clear() {
    synchronized (f_counts) {
      f_counts.clear();
    }
  }
}
