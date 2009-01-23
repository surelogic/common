package com.surelogic.common;

import java.io.InputStream;

/**
 * An input stream whose {@link #close} method does not throw an exception.
 */
public abstract class SafeCloseInputStream extends InputStream {
  @Override
  public abstract void close();
}
