package com.surelogic.common.ui.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.AbstractJavaZip;
import com.surelogic.common.ISourceZipFileHandles;
import com.surelogic.common.SLUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;

public abstract class AbstractHistoricalSourceView extends ViewPart {

	// FIX replace with SourceViewer?
	private StyledText f_source;
	private JavaSyntaxHighlighter f_highlighter;
	private ISourceZipFileHandles f_lastSources = null;
	private String f_lastType = null;

	private Label f_sourceLabel;
	private Date f_sourceCopyTime = null;

	@Override
	public void createPartControl(final Composite parent) {
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = gl.verticalSpacing = 0;
		gl.marginHeight = gl.marginWidth = 0;
		parent.setLayout(gl);
		f_sourceLabel = new Label(parent, SWT.NONE);
		f_sourceLabel.setText(" ");
		f_sourceLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));

		f_source = new StyledText(parent, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER | SWT.READ_ONLY);
		f_source.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		f_highlighter = new JavaSyntaxHighlighter(f_source.getDisplay());

		clearSourceCodeFromView();
	}

	@Override
	public void setFocus() {
		f_source.setFocus();
	}

	/**
	 * Clears out the source code from this view.
	 */
	public final void clearSourceCodeFromView() {
		f_source.setText(" ");
		f_sourceLabel.setText("No source code to show.");
		f_sourceCopyTime = null;
	}

	/**
	 * Sets the time when the displayed source code was snapshot. This is
	 * displayed to the user.
	 * 
	 * @param when
	 *            a time. A {@code null} value means that the snapshot time is
	 *            unknown.
	 */
	public final void setSourceSnapshotTime(Date when) {
		f_sourceCopyTime = when;
	}

	/**
	 * Gets the time when the displayed source code was snapshot.
	 * 
	 * @return a time. A {@code null} value means that the snapshot time is
	 *         unknown.
	 */
	public Date getSourceSnapshotTime() {
		return f_sourceCopyTime;
	}

	/*
	 * Pattern matching an anonymous class in a type signature
	 */
	private static final Pattern ANON = Pattern.compile("[.$]\\d+");
	/*
	 * Pattern matching an anonymous class in a type signature, as well as
	 * everything that follows
	 */
	private static final Pattern ANON_ONWARDS = Pattern.compile("[.$]\\d.*");

	/**
	 * @return true if the view is populated
	 */
	private boolean showSourceFile(final ISourceZipFileHandles sources,
			String qname) {
		if (qname == null) {
			return false;
		}
		// FIXME We use ANON_ONWARDS, b/c we don't record any type location info
		// about types defined within an anonymous class
		qname = ANON_ONWARDS.matcher(qname).replaceFirst("");
		if (f_lastSources == sources && f_lastType == qname) {
			return true; // Should be populated from before
		}
		for (final File f : sources.getSourceZips()) {
			try {
				final ZipFile zf = new ZipFile(f);
				try {
					final Map<String, String> fileMap = AbstractJavaZip
							.readClassMappings(zf);
					if (fileMap != null) {
						final String path = fileMap.get(qname);
						if (path != null) {
							populate(zf, path);
							f_lastSources = sources;
							f_lastType = qname;
							return true;
						}
					}
				} finally {
					zf.close();
				}
			} catch (final Exception e) {
				SLLogger.getLogger().log(Level.WARNING,
						"Unexcepted exception trying to read a source file", e);
			}
		}
		return false;
	}

	private void populate(final ZipFile zf, final String path)
			throws IOException {
		final ZipEntry ze = zf.getEntry(path);
		InputStream in = zf.getInputStream(ze);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		// Count the number of lines
		int numLines = 0;
		while (br.readLine() != null) {
			numLines++;
		}
		final int spacesForLineNum = Integer.toString(numLines).length();
		final StringBuilder sb = new StringBuilder();
		int lineNum = 1;
		String line;

		// Re-init to really read
		in = zf.getInputStream(ze);
		br = new BufferedReader(new InputStreamReader(in));
		while ((line = br.readLine()) != null) {
			final String lineStr = Integer.toString(lineNum);
			for (int i = lineStr.length(); i < spacesForLineNum; i++) {
				sb.append(' ');
			}
			sb.append(lineStr).append(' ').append(line).append('\n');
			lineNum++;
		}
		/*
		 * char[] buf = new char[4096]; int read; while ((read = br.read(buf))
		 * >= 0) { sb.append(buf, 0, read); }
		 */
		f_source.setFont(JFaceResources.getTextFont());
		f_source.setText(sb.toString());
		f_source.setStyleRanges(f_highlighter.computeRanges(f_source.getText()));

		String sourceInfo = path
				+ (f_sourceCopyTime == null ? "" : " at "
						+ SLUtility.toStringHMS(f_sourceCopyTime));
		f_sourceLabel.setText(sourceInfo);
	}

	protected abstract ISourceZipFileHandles findSources(String run);

	protected static void tryToOpenInEditor(final Class<?> viewClass,
			final String run, final String pkg, final String type,
			int lineNumber) {
		final AbstractHistoricalSourceView view = (AbstractHistoricalSourceView) EclipseUIUtility
				.showView(viewClass.getName(), null,
						IWorkbenchPage.VIEW_VISIBLE);
		//System.out.println("View = "+view);
		if (view != null) {
			ISourceZipFileHandles sources = view.findSources(run);
			if (sources != null) {
				final boolean loaded = view.showSourceFile(sources,
						pkg == null || pkg.length() == 0 ? type : pkg + '.' + type);
				//System.out.println("Loading "+type+": "+loaded);
				if (loaded) {
					/*
					 * The line numbers passed to this method are typically 1
					 * based relative to the first line of the content. We need
					 * to change this to be 0 based.
					 */
					if (lineNumber > 0) {
						lineNumber--;
					}
					if (lineNumber < 0) {
						lineNumber = 0;
					}
					view.showAndSelectLine(lineNumber);
				}
			}
		}
	}

	protected static void tryToOpenInEditorUsingMethodName(
			final Class<?> viewClass, final String run, final String pkg,
			final String type, final String method) {
		final AbstractHistoricalSourceView view = (AbstractHistoricalSourceView) EclipseUIUtility
				.showView(viewClass.getName(), null,
						IWorkbenchPage.VIEW_VISIBLE);
		if (view != null) {
			ISourceZipFileHandles sources = view.findSources(run);
			if (sources != null) {
				final boolean loaded = view.showSourceFile(sources,
						pkg == null ? type : pkg + '.' + type);
				if (loaded) {
					final int lineNumber = computeLineFromMethod(
							view.f_source.getText(), type, method);
					view.showAndSelectLine(lineNumber);
				}
			}
		}
	}

	protected static void tryToOpenInEditorUsingFieldName(
			final Class<?> viewClass, final String run, final String pkg,
			final String type, final String field) {
		final AbstractHistoricalSourceView view = (AbstractHistoricalSourceView) EclipseUIUtility
				.showView(viewClass.getName(), null,
						IWorkbenchPage.VIEW_VISIBLE);
		if (view != null) {
			ISourceZipFileHandles sources = view.findSources(run);
			if (sources != null) {
				final boolean loaded = view.showSourceFile(sources,
						pkg == null ? type : pkg + '.' + type);
				if (loaded) {
					final int lineNumber = computeLineFromField(
							view.f_source.getText(), type, field);
					view.showAndSelectLine(lineNumber);
				}
			}
		}
	}

	/**
	 * Shows the passed line number in the passed view and highlights the line
	 * via selection.
	 * 
	 * @param view
	 *            the historical source view.
	 * @param lineNumber
	 *            index of the line, 0 based relative to the first line in the
	 *            content.
	 */
	private void showAndSelectLine(final int lineNumber) {
		/*
		 * Show the line, move up a bit if we can.
		 */
		f_source.setTopIndex(lineNumber < 5 ? 0 : lineNumber - 5);

		if (lineNumber < 0) {
			SLLogger.getLogger().info(
					"Line number is too small for HistoricalSourceView: "
							+ lineNumber);
			return;
		}
		/*
		 * Highlight the line by selecting it in the widget.
		 */
		try {
			final int start = f_source.getOffsetAtLine(lineNumber);
			final int end;
			if (lineNumber + 1 > f_source.getLineCount()) {
				end = f_source.getCharCount();
			} else {
				end = f_source.getOffsetAtLine(lineNumber + 1);
			}
			f_source.setSelection(start, end);
		} catch (IllegalArgumentException e) {
			SLLogger.getLogger().log(
					Level.INFO,
					"Could not find line " + lineNumber
							+ " in HistoricalSourceView", e);
		}
	}

	private static int computeLineFromMethod(final String source,
			final String type, final String method) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		parser.setUnitName("temp____");
		parser.setProject(null);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> decls = cu.types();
		for (AbstractTypeDeclaration decl : decls) {
			// decl.accept(new ASTVisitorRecorder());
			MethodFinderVisitor v = new MethodFinderVisitor(type, method);
			decl.accept(v);
			if (v.getPosition() != 0) {
				// Subtract one because we are zero-based instead of one-based.
				return cu.getLineNumber(v.getPosition()) - 1;
			}
		}
		return cu.getLineNumber(0) - 1;

	}

	private static int computeLineFromField(final String source,
			final String type, final String field) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		parser.setUnitName("temp____");
		parser.setProject(null);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> decls = cu.types();
		for (AbstractTypeDeclaration decl : decls) {
			// decl.accept(new ASTVisitorRecorder());
			FieldFinderVisitor v = new FieldFinderVisitor(type, field);
			decl.accept(v);
			if (v.getPosition() != 0) {
				// Subtract one because we are zero-based instead of one-based.
				return cu.getLineNumber(v.getPosition()) - 1;
			}
		}
		return cu.getLineNumber(0) - 1;
	}

	/**
	 * Visitor that keeps track of type information.
	 * 
	 * @author nathan
	 * 
	 */
	private static abstract class TypeVisitor extends ASTVisitor {
		protected final LinkedList<String> typeList;
		protected final LinkedList<String> currentType;

		TypeVisitor(final String type) {
			typeList = new LinkedList<String>();
			for (String typePart : ANON.matcher(type).replaceAll("")
					.split("[.$]")) {
				typeList.addFirst(typePart);
			}
			currentType = new LinkedList<String>();
		}

		@Override
		public void endVisit(final TypeDeclaration node) {
			currentType.removeFirst();
		}

		@Override
		public boolean visit(final TypeDeclaration node) {
			currentType.addFirst(node.getName().getIdentifier());
			return true;
		}

	}

	private static class MethodFinderVisitor extends TypeVisitor {
		private final String method;
		private int position;
		private int possiblePosition;
		private int unlikelyPosition;

		MethodFinderVisitor(final String type, final String method) {
			super(type);
			this.method = method;
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (method.equals(node.getName().getIdentifier())
					|| method.equals("<init>") && node.isConstructor()) {
				int nodePosition = node.getStartPosition();
				if (currentType.equals(typeList)) {
					// Definite match, but don't update if we already have a
					// result b/c we want the first declared constructor
					if (position <= 0) {
						position = nodePosition;
					}
				} else if (currentType.containsAll(typeList)) {
					// Possible match. The parser sometimes gets confused
					// about when types begin and end, so since this type
					// hierarchy does include all of the types in our target
					// hierarchy we will use it.
					possiblePosition = nodePosition;
				} else {
					// Probably not our match, but if we don't find any other
					// matching fields we will use it as a best guess.
					unlikelyPosition = nodePosition;
				}
			}
			return true;
		}

		/**
		 * Returns the most likely position of the field
		 * 
		 * @return
		 */
		public int getPosition() {
			return position != 0 ? position
					: possiblePosition != 0 ? possiblePosition
							: unlikelyPosition;
		}
	}

	/**
	 * {@link ASTVisitor} that finds a given field in a given type.
	 */
	private static class FieldFinderVisitor extends TypeVisitor {
		private final String field;
		private int position;
		private int possiblePosition;
		private int unlikelyPosition;
		private boolean inField;

		/**
		 * Construct a new field finder
		 * 
		 * @param type
		 *            a type signature. It may contain anonymous classes and use
		 *            either $ or . as a delimiter.
		 * @param field
		 *            the field name
		 */
		FieldFinderVisitor(final String type, final String field) {
			super(type);
			this.field = field;
		}

		@Override
		public boolean visit(final AnonymousClassDeclaration node) {
			return super.visit(node);
		}

		@Override
		public boolean visit(final FieldDeclaration node) {
			inField = true;
			return true;
		}

		@Override
		public void endVisit(final FieldDeclaration node) {
			inField = false;
		}

		@Override
		public boolean visit(final VariableDeclarationFragment node) {
			if (inField && node.getName().getIdentifier().equals(field)) {
				int nodePosition = node.getStartPosition();
				if (currentType.equals(typeList)) {
					// Definite match
					position = nodePosition;
				} else if (currentType.containsAll(typeList)) {
					// Possible match. The parser sometimes gets confused
					// about when types begin and end, so since this type
					// hierarchy does include all of the types in our target
					// hierarchy we will use it.
					possiblePosition = nodePosition;
				} else {
					// Probably not our match, but if we don't find any other
					// matching fields we will use it as a best guess.
					unlikelyPosition = nodePosition;
				}

			}
			return true;
		}

		/**
		 * Returns the most likely position of the field
		 * 
		 * @return
		 */
		public int getPosition() {
			return position != 0 ? position
					: possiblePosition != 0 ? possiblePosition
							: unlikelyPosition;
		}

	}

	/**
	 * Prints out a representation of the Abstract Syntax Tree.
	 */
	static class ASTVisitorRecorder extends ASTVisitor {

		int tabs;

		@Override
		public void postVisit(final ASTNode node) {
			tabs--;
		}

		@Override
		public void preVisit(final ASTNode node) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < tabs; i++) {
				b.append('\t');
			}
			b.append(ASTNode.nodeClassForType(node.getNodeType())
					.getSimpleName());
			System.out.println(b.toString());
			tabs++;
		}

		@Override
		public boolean visit(final Block node) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < tabs; i++) {
				b.append('\t');
			}
			b.append(node.statements().size());
			System.out.println(b.toString());
			return true;
		}

	}

}
