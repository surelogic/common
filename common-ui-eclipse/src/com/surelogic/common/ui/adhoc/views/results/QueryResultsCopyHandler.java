package com.surelogic.common.ui.adhoc.views.results;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.surelogic.common.ui.EclipseUIUtility;

public class QueryResultsCopyHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    final Clipboard cb = new Clipboard(EclipseUIUtility.getShell().getDisplay());
    try {
      return execute(event, cb);
    } finally {
      cb.dispose();
    }
  }

  private Object execute(final ExecutionEvent event, final Clipboard cb) {
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (part instanceof AbstractQueryResultsView) {
      final AbstractQueryResultsView view = (AbstractQueryResultsView) part;
      AbstractQueryResultsView.copySelection(view.getResult());
    }
    return null;
  }
}
