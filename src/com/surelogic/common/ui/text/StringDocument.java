package com.surelogic.common.ui.text;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;

public class StringDocument extends AbstractDocument {
  public StringDocument(String text) {
    setTextStore(new StringBufferTextStore(text));
    ILineTracker tracker = new DefaultLineTracker();
    tracker.set(text);
    setLineTracker(tracker);
    completeInitialization();
  }
}