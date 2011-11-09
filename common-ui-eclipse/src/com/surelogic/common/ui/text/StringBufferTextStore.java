package com.surelogic.common.ui.text;

import org.eclipse.jface.text.ITextStore;

public class StringBufferTextStore implements ITextStore {
  final StringBuffer buf;

  public StringBufferTextStore(String text) {
    buf = new StringBuffer(text);
  }

  public char get(int offset) {
    return buf.charAt(offset);
  }

  public String get(int offset, int length) {
    return buf.substring(offset, offset + length);
  }

  public int getLength() {
    return buf.length();
  }

  public void replace(int offset, int len, String text) {
    buf.replace(offset, offset + len, text);
  }

  public void set(String text) {
    buf.replace(0, buf.length(), text);
  }
}
