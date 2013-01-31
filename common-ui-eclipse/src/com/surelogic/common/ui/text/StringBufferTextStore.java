package com.surelogic.common.ui.text;

import org.eclipse.jface.text.ITextStore;

public class StringBufferTextStore implements ITextStore {
  final StringBuffer buf;

  public StringBufferTextStore(String text) {
    buf = new StringBuffer(text);
  }

  @Override
  public char get(int offset) {
    return buf.charAt(offset);
  }

  @Override
  public String get(int offset, int length) {
    return buf.substring(offset, offset + length);
  }

  @Override
  public int getLength() {
    return buf.length();
  }

  @Override
  public void replace(int offset, int len, String text) {
    buf.replace(offset, offset + len, text);
  }

  @Override
  public void set(String text) {
    buf.replace(0, buf.length(), text);
  }
}
