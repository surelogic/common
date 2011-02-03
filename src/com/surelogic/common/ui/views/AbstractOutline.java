/*
 * Created on Jan 25, 2005
 *
 */
package com.surelogic.common.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.views.contentoutline.*;

public abstract class AbstractOutline extends ContentOutline {

	@Override
	protected final PageRec doCreatePage(IWorkbenchPart part) {
		// Try to get an outline page.
		Class<?> c = getPageType();
		Object obj = part.getAdapter(c);
		if (c.isInstance(obj)) {
			IContentOutlinePage page = (IContentOutlinePage) obj;
			return setupPage(part, page);
		} else if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IContentOutlinePage p = makePage(file);
				if (p != null) {
					return setupPage(part, p);
				}
			}
		}
		// There is no content outline
		return null;
	}

	private PageRec setupPage(IWorkbenchPart part, IContentOutlinePage page) {
		if (page instanceof IPageBookViewPage)
			initPage((IPageBookViewPage) page);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	protected abstract Class<?> getPageType();

	protected abstract IContentOutlinePage makePage(IFile file);
}
