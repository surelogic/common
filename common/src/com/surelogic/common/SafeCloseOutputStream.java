package com.surelogic.common;

import java.io.OutputStream;


/**
 * An output stream whose {@link #close} method does not throw an exception.
 */
public abstract class SafeCloseOutputStream extends OutputStream {
  @Override
  public abstract void close();
}
