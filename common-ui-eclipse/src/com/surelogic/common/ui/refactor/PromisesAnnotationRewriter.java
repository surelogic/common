package com.surelogic.common.ui.refactor;

import java.util.*;
import java.util.logging.Level;

import org.apache.commons.collections15.*;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import com.surelogic.common.AnnotationConstants;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.refactor.AnnotationDescription;
import com.surelogic.common.refactor.Field;
import com.surelogic.common.refactor.IJavaDeclaration;
import com.surelogic.common.refactor.Method;
import com.surelogic.common.refactor.MethodParameter;
import com.surelogic.common.refactor.TypeContext;

public class PromisesAnnotationRewriter {

	private static final String PROMISE_PKG = "com.surelogic";
	private final ASTParser parser;
	private ASTNode ast;
	private ASTRewrite rewrite;
	private final Set<String> allowsMultipleAnnos;
	
	  /**
	   * @param annos A set of annotation names that allow multiple annotations on a given declaration
	   */
	public PromisesAnnotationRewriter(Set<String> annos) {
		parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		allowsMultipleAnnos = annos;
	}
	
	public PromisesAnnotationRewriter() {
		this(null);		
	}

	/**
	 * Set the focused compilation unit. All rewrite methods target this
	 * compilation unit.
	 * 
	 * @param cu
	 */
	public void setCompilationUnit(final ICompilationUnit cu) {
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		ast = parser.createAST(null);
		rewrite = ASTRewrite.create(ast.getAST());
	}

	public void writeAnnotations(final Set<AnnotationDescription> descs) {
		writeAnnotations(descs, null);
	}

	public void writeAnnotations(final Set<AnnotationDescription> descs,
			final TextEditGroup editGroup) {
		ast.accept(new AnnotationVisitor(descs, editGroup, false));
	}

	public void writeAssumptions(final Set<AnnotationDescription> descs) {
		ast.accept(new AnnotationVisitor(descs, null, true));
	}

	/**
	 * Produce a TextEdit representing the set of rewrites made to this
	 * compilation unit
	 * 
	 * @return
	 */
	public TextEdit getTextEdit() {
		try {
			return rewrite.rewriteAST();
		} catch (final JavaModelException e) {
			throw new IllegalStateException(e);
		} catch (final IllegalArgumentException e) {
			throw new IllegalStateException(e);
		}
	}

	private class AnnotationVisitor extends ASTVisitor {
		private final Map<IJavaDeclaration, List<AnnotationDescription>> targetMap;
		private final MultiMap<TypeContext,IJavaDeclaration> createMap;
		private final TextEditGroup editGroup;
		private Method inMethod;
		private TypeContext type;
		private final boolean isAssumption;
		private final Set<String> imports;

		public AnnotationVisitor(final Collection<AnnotationDescription> descs,
				final TextEditGroup editGroup, final boolean isAssumption) {
			targetMap = new HashMap<IJavaDeclaration, List<AnnotationDescription>>();
			createMap = new MultiHashMap<TypeContext, IJavaDeclaration>();
			
			for (final AnnotationDescription desc : descs) {
				final IJavaDeclaration target = isAssumption ? desc.getAssumptionTarget()
						: desc.getTarget();
				if (!isAssumption && target.isImplicit()) {				
					// We need to create the declaration before adding the annotation
					createMap.put(target.getTypeContext(), target);
				} 
				
				List<AnnotationDescription> list = targetMap.get(target);
				if (list == null) {
					list = new ArrayList<AnnotationDescription>();
					targetMap.put(target, list);
				}
				list.add(desc);
			}
			this.isAssumption = isAssumption;
			this.editGroup = editGroup;
			this.imports = new HashSet<String>();
		}

		private void checkForDeclsToCreate(AbstractTypeDeclaration node) {
			final Collection<IJavaDeclaration> decls = createMap.get(type);
			if (decls == null || decls.isEmpty()) {
				return;
			}
			final List<BodyDeclaration> toAdd = new ArrayList<BodyDeclaration>();
			final List<AnnotationDescription> scopedPromises = new ArrayList<AnnotationDescription>();
			final AST ast = rewrite.getAST();
			for(IJavaDeclaration decl : decls) {
				final Method m = (Method) decl;
				final String name = m.getMethod();
				if (name.equals(node.getName().toString())) {
					// Creating a constructor
					MethodDeclaration md = ast.newMethodDeclaration();
					md.setName(ast.newSimpleName(name));
					md.setConstructor(true);							
					
					Block blk = ast.newBlock();
					// TODO set "nothing to do" comment
					md.setBody(blk);	
					
					// Set to be public
					ListRewrite lrw = rewrite.getListRewrite(md, md.getModifiersProperty());
					for(Object o : ast.newModifiers(Modifier.PUBLIC)) {
						lrw.insertFirst((Modifier) o, null);
					}
					// TODO add throws clause					
					toAdd.add(md);
					
					// Add annotations to the newly created method
					List<AnnotationDescription> annos = targetMap.remove(decl);
					rewriteNode(md, md.getModifiersProperty(), annos, decl);
				} else {	
					// Convert annos on the implicit method into a scoped promise on the enclosing type
					final List<AnnotationDescription> annos = targetMap.remove(decl);					
					for(AnnotationDescription a : annos) {
						final String contents = computeScopedPromiseContents(a, m);
						AnnotationDescription a2 = new AnnotationDescription("Promise", contents, decl);
						scopedPromises.add(a2);
					}					
				}		
			}
			if (!toAdd.isEmpty()) {
				ASTNode group = rewrite.createGroupNode(toAdd.toArray(new ASTNode[toAdd.size()]));
				ListRewrite lrw = rewrite.getListRewrite(node, node.getBodyDeclarationsProperty());
				lrw.insertFirst(group, null);
			}
			if (!scopedPromises.isEmpty()) {
				rewriteNode(node, node.getModifiersProperty(), scopedPromises, null);
			}
		}
		
		private String computeScopedPromiseContents(AnnotationDescription a, Method m) {
			final StringBuilder sb = new StringBuilder("@");
			sb.append(a.getAnnotation()).append('(').append(a.getContents()).append(") for ");
			sb.append(m.getMethod()).append('(');
			boolean first = true;
			for(String param : m.getParams()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(param);
			}
			sb.append(')');
			return sb.toString();
		}
		
		@Override
		public boolean visit(final TypeDeclaration node) {
			typeContext(node.getName().getIdentifier());
			rewriteNode(node, TypeDeclaration.MODIFIERS2_PROPERTY, targetMap
					.remove(type), type);
			checkForDeclsToCreate(node);
			return true;
		}

		@Override
		public boolean visit(final AnnotationTypeDeclaration node) {
			typeContext(node.getName().getIdentifier());
			rewriteNode(node, AnnotationTypeDeclaration.MODIFIERS2_PROPERTY,
					targetMap.remove(type), type);
			checkForDeclsToCreate(node);
			return true;
		}

		@Override
		public boolean visit(final AnonymousClassDeclaration node) {
			final String name = "ANON"; // FIXME
			typeContext(name);
			// TODO checkForDeclsToCreate(node);
			return true;
		}

		@Override
		public boolean visit(final EnumDeclaration node) {
			typeContext(node.getName().getIdentifier());
			rewriteNode(node, EnumDeclaration.MODIFIERS2_PROPERTY, targetMap
					.remove(type), type);
			checkForDeclsToCreate(node);
			return true;
		}

		private void typeContext(final String name) {
		
			if (type == null) {
				type = new TypeContext(name);
			} else if (inMethod == null) {
				type = new TypeContext(type, name);
			} else {
				type = new TypeContext(inMethod, name);
				inMethod = null;
			}
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			final IMethodBinding mB = node.resolveBinding();
			final ITypeBinding[] paramDecls = mB.getParameterTypes();
			final String[] params = new String[paramDecls.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = fromType(paramDecls[i]);
			}
			inMethod = new Method(type, node.getName().getIdentifier(), params, false); // TODO Should this ever be true?
			//System.out.println("Looking at "+inMethod);
			rewriteNode(node, MethodDeclaration.MODIFIERS2_PROPERTY, targetMap
					.remove(inMethod), inMethod);
			
			handleParameters(node);
			return true;
		}
		
		private void handleParameters(final MethodDeclaration m) {
			int i = 0;
			for(Object o : m.parameters()) {
				SingleVariableDeclaration p = (SingleVariableDeclaration) o;
				MethodParameter inParameter = new MethodParameter(inMethod, i);				
				rewriteNode(p, SingleVariableDeclaration.MODIFIERS2_PROPERTY,
						targetMap.remove(inParameter), inParameter);
				i++;
			}
		}
		
		/**
		 * Rewrite the given node property to include this list of annotations
		 * 
		 * @param node
		 * @param prop
		 * @param list
		 */
		private void rewriteNode(final ASTNode node,
				final ChildListPropertyDescriptor prop,
				final List<AnnotationDescription> list,
				final IJavaDeclaration target) {
			if (list != null) {
				Collections.sort(list);
				Collections.reverse(list);
				final List<List<AnnotationDescription>> anns = new ArrayList<List<AnnotationDescription>>();
				List<AnnotationDescription> cur = null;
				String curAnn = null;
				for (final AnnotationDescription d : list) {
					if (!d.getAnnotation().equals(curAnn)) {
						cur = new ArrayList<AnnotationDescription>();
						anns.add(cur);
						curAnn = d.getAnnotation();
					}
					cur.add(d);
				}
				final ListRewrite lrw = rewrite.getListRewrite(node, prop);
				for (final List<AnnotationDescription> ann : anns) {
					mergeAnnotations(node.getAST(), ann, lrw, target);
				}
			}
		}

		/**
		 * Merge the list of annotations into the given rewrite. This can
		 * incorporate or replace annotations that currently exist in the
		 * compilation unit.
		 * 
		 * @param ann
		 * @param lrw
		 * @param target
		 */
		@SuppressWarnings("unchecked")
		private void mergeAnnotations(final AST ast,
				final List<AnnotationDescription> ann, final ListRewrite lrw,
				final IJavaDeclaration target) {
			final List<ASTNode> nodes = lrw.getRewrittenList();
			final Mergeable m = merge(ann);
			for (final ASTNode aNode : nodes) {
				if (aNode instanceof Annotation) {
					final Annotation a = (Annotation) aNode;
					if (m.match(a)) {
						lrw.replace(aNode, m.merge(ast, a, target, imports),
								editGroup);
						return;
					}
				}
			}
			lrw.insertFirst(m.merge(ast, null, target, imports), editGroup);
		}

		private String fromType(final ITypeBinding t) {
			return t.getQualifiedName().replaceAll("<.*>", "");
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(final FieldDeclaration node) {
			final List<VariableDeclarationFragment> fragments = node
					.fragments();
			// Handle when we have more than one field in the same declaration
			final List<AnnotationDescription> list = new ArrayList<AnnotationDescription>();
			Field f = null;
			for (final VariableDeclarationFragment frag : fragments) {
				f = new Field(type, frag.getName().getIdentifier());
				final List<AnnotationDescription> list2 = targetMap.remove(f);
				if (list2 != null) {
					list.addAll(list2);
				}
			}
			if (!list.isEmpty()) {
				rewriteNode(node, FieldDeclaration.MODIFIERS2_PROPERTY, list, f);
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void endVisit(final CompilationUnit node) {
			// Add any new imports
			final List<ImportDeclaration> importNodes = node.imports();

			// Check for existing imports	
			for (final ImportDeclaration i : importNodes) {
				final String qname = i.getName().getFullyQualifiedName();
				if (PROMISE_PKG.equals(qname)) {
					// This should handle any promises (unless ambiguous? TODO)
					super.endVisit(node);
					return;
				}
				imports.remove(qname);
			}
			if (imports.size() > 0) {
				final AST ast = node.getAST();
				final ListRewrite lrw = rewrite.getListRewrite(node,
						CompilationUnit.IMPORTS_PROPERTY);
				for (final String i : imports) {
					final ImportDeclaration d = ast.newImportDeclaration();
					d.setName(ast.newName(i));
					lrw.insertLast(d, null);
				}
			}
			super.endVisit(node);
		}

		@Override
		public void endVisit(final MethodDeclaration node) {
			if (inMethod == null) {
				SLLogger.getLoggerFor(PromisesAnnotationRewriter.class).log(
						Level.SEVERE, "Unexpected method syntax");
			}
			inMethod = null;
		}

		private void endTypeContext() {
			if (type.getMethod() != null) {
				inMethod = type.getMethod();
			}
			type = type.getParent();
		}

		@Override
		public void endVisit(final TypeDeclaration node) {
			endTypeContext();
		}

		@Override
		public void endVisit(final AnnotationTypeDeclaration node) {
			endTypeContext();
		}

		@Override
		public void endVisit(final AnonymousClassDeclaration node) {
			endTypeContext();
		}

		@Override
		public void endVisit(final EnumDeclaration node) {
			endTypeContext();
		}

		Mergeable merge(final List<AnnotationDescription> descs) {
			if (isAssumption) {
				return new AssumptionMergeStrategy(descs);
			} else {
				final String name = descs.get(0).getAnnotation();
				if (AGGREGATE.equals(name) || REQUIRESLOCK.equals(name)) {
					return new CommaDelimitedMergeStrategy(name, descs);
				}
				return new DefaultMergeStrategy(descs);
			}
		}

	}

	private static final String ASSUME = "Assume";
	private static final String ASSUMES = "Assumes";

	abstract class AbstractMergeable implements Mergeable {
		@SuppressWarnings("unchecked")
		protected void extractExistingAnnos(final Annotation cur, final Set<String> contents) {		
			final Expression e = extractValue(cur);
			if (e instanceof StringLiteral) {
				final String lit = ((StringLiteral) e).getLiteralValue();
				handleExistingAnno(lit, contents);
			} else if (e instanceof ArrayInitializer) {
				final ArrayInitializer init = (ArrayInitializer) e;
				final List<Expression> es = init.expressions();
				for (final Expression ex : es) {
					if (ex instanceof Annotation) {
						extractExistingAnnos((Annotation) ex, contents);
					}
				}
			} else if (e instanceof InfixExpression) {
				final InfixExpression ex = (InfixExpression) e;
				if (ex.getOperator() == Operator.PLUS) {
					// Assume to be a string concatenation
					StringBuilder sb = new StringBuilder();
					sb.append(extractString(ex.getLeftOperand()));
					sb.append(extractString(ex.getRightOperand()));
					if (ex.hasExtendedOperands()) {
						for(Object o : ex.extendedOperands()) {
							sb.append(extractString((Expression) o));
						}
					}
				}
			}
		}

		private String extractString(Expression e) {
			if (e instanceof StringLiteral) {
				return ((StringLiteral) e).getLiteralValue();
			}
			SLLogger.getLogger().warning("Unexpected expression in anno: "+e);
			return e.toString();
		}
		
		protected void handleExistingAnno(String s, Set<String> contents) {
			// Default thing to do
			contents.add(s);
		}
	}
	
	/**
	 * Creates assumptions for the annotations
	 */
	class AssumptionMergeStrategy extends AbstractMergeable {

		final List<AnnotationDescription> newAnnotations;

		AssumptionMergeStrategy(final List<AnnotationDescription> anns) {
			this.newAnnotations = new ArrayList<AnnotationDescription>(anns);
		}

		public Annotation merge(final AST ast, final Annotation cur,
				final IJavaDeclaration target, final Set<String> imports) {
			final Set<String> existing = new HashSet<String>();
			if (cur != null) {
				extractExistingAnnos(cur, existing);
			}
			for (final AnnotationDescription desc : newAnnotations) {
				addImport(ASSUME, imports);
				existing.add(String
						.format("%s for %s in %s", desc.toString(), desc
								.getTarget().forSyntax(), desc.getCU()
								.getPackage()));
			}
			final List<String> sortedAnns = new ArrayList<String>(existing);
			Collections.sort(sortedAnns);
			final List<Annotation> anns = new ArrayList<Annotation>(sortedAnns
					.size());
			for (final String s : sortedAnns) {
				final SingleMemberAnnotation ann = ast
						.newSingleMemberAnnotation();
				ann.setTypeName(ast.newName(ASSUME));
				final StringLiteral lit = ast.newStringLiteral();
				lit.setLiteralValue(s);
				ann.setValue(lit);
				anns.add(ann);
			}
			return createWrappedAssume(ast, anns, imports);
		}

		public boolean match(final Annotation a) {
			final String aName = a.getTypeName().getFullyQualifiedName()
					.replaceAll(".*\\.", "");
			return ASSUME.equals(aName) || ASSUMES.equals(aName);
		}
	}

	private static final String REQUIRESLOCK = "RequiresLock";
	private static final String AGGREGATE = "Aggregate";

	/**
	 * Combines the annotations into a one, separated by commas
	 */
	class CommaDelimitedMergeStrategy extends AbstractMergeable {

		final List<AnnotationDescription> newAnnotations;
		final String name;

		CommaDelimitedMergeStrategy(final String name,
				final List<AnnotationDescription> anns) {
			this.newAnnotations = anns;
			this.name = name;
		}

		public boolean match(final Annotation a) {
			final String aName = a.getTypeName().getFullyQualifiedName()
					.replaceAll(".*\\.", "");
			return name.equals(aName);
		}

		public Annotation merge(final AST ast, final Annotation a,
				final IJavaDeclaration target, final Set<String> imports) {
			addImport(name, imports);
			final TreeSet<String> contents = new TreeSet<String>();
			for (final AnnotationDescription desc : newAnnotations) {
				if (desc.getContents() != null) {
					for (final String s : desc.getContents().split(",")) {
						contents.add(s.trim());
					}
				}
			}
			if (a != null) {
				extractExistingAnnos(a, contents);
			}
			if (contents.isEmpty()) {
				final MarkerAnnotation ma = ast.newMarkerAnnotation();
				ma.setTypeName(ast.newName(name));
				return ma;
			} else {
				final SingleMemberAnnotation ann = ast
						.newSingleMemberAnnotation();
				ann.setTypeName(ast.newName(name));
				final StringLiteral lit = ast.newStringLiteral();
				lit.setLiteralValue(join(contents));
				ann.setValue(lit);
				return ann;
			}
		}
		@Override
		protected void handleExistingAnno(String ss, Set<String> contents) {
			if (ss != null && ss.length() > 0) {
				for (final String s : ss.split(",")) {
					contents.add(s.trim());
				}
			}
		}
	}

	/**
	 * Adds the annotation, wrapping the whole thing in a 'plural' annotation if needed
	 */
	class DefaultMergeStrategy extends AbstractMergeable {

		final String name;
		final boolean allowsMultiple;
		final String wrapper;
		final Set<AnnotationDescription> newAnnotations;
		final Map<String,AnnotationDescription> altAnnotations;
		
		DefaultMergeStrategy(final List<AnnotationDescription> anns) {
			this.name = anns.get(0).getAnnotation();
			allowsMultiple = allowsMultipleAnnos != null && allowsMultipleAnnos.contains(name);			
			
			// Figure out which set the annos need to be in
			// (allocating the sets on demand)
			Set<AnnotationDescription> newA = Collections.emptySet();
			Set<AnnotationDescription> altA = Collections.emptySet();
			for(AnnotationDescription a : anns) {
				final boolean replacing = a.getReplacedContents() != null;
				Set<AnnotationDescription> set = replacing ? altA : newA;								
				if (set.isEmpty()) {
					set = new HashSet<AnnotationDescription>();
					if (replacing) {
						altA = set;
					} else {
						newA = set;
					}
				}
				set.add(a);
			}
			if (!allowsMultiple) {
				// TODO sanity checks?
			}
			this.newAnnotations = newA;
			if (altA.isEmpty()) {
				altAnnotations = Collections.emptyMap();
			} else {
				altAnnotations = new HashMap<String,AnnotationDescription>(altA.size());
				for(AnnotationDescription a : altA) {
					AnnotationDescription old = altAnnotations.put(a.getReplacedContents(), a);
					if (old != null) {
						SLLogger.getLogger().warning("Multiple replacements for "+a.getReplacedContents());	
					}
				}
			}
			this.wrapper = name + "s";
		}

		public Annotation merge(final AST ast, final Annotation cur,
				final IJavaDeclaration target, final Set<String> imports) {
			final Set<String> newContents = new HashSet<String>();
			for (final AnnotationDescription desc : newAnnotations) {
				newContents.add(desc.getContents());
			}
			if (cur != null) {
				extractExistingAnnos(cur, newContents);
			} else {
				// check if we're supposed to replace something
				for (final String alt : altAnnotations.keySet()) {				
					SLLogger.getLogger().warning("Not replacing @"+name+"("+alt+") as expected");					
				}
			}			
			return createWrappedAnnotation(ast, name, wrapper, newContents,
					imports);
		}

		@Override
		protected void handleExistingAnno(String s, Set<String> contents) {
			if (altAnnotations.isEmpty()) {				
				if (allowsMultiple || contents.isEmpty()) {
					contents.add(s);
				} else {
					// This must be a replacement, so ignore s				
				}
				return;
			}			
			AnnotationDescription alt;
			if (allowsMultiple) {
				alt = altAnnotations.get(s);			
			} else {
				// TODO check if there's more than one?
				alt = altAnnotations.values().iterator().next();
			}
			if (alt != null) {
				contents.add(alt.getContents());
			} else {
				contents.add(s);
			}
		}
		
		public boolean match(final Annotation a) {
			final String aName = a.getTypeName().getFullyQualifiedName()
					.replaceAll(".*\\.", "");
			return name.equals(aName) || wrapper.equals(aName);
		}
	}

	interface Mergeable {
		/**
		 * Whether or not this mergeable object matches.
		 * 
		 * @param a
		 * @return
		 */
		boolean match(Annotation a);

		/**
		 * Produce a new annotation containing the annotations held in this
		 * mergeable object, as well as the given annotation.
		 * 
		 * @param a
		 *            The annotation to merge in. May be null.
		 * @param imports
		 *            the set of imports used. This should be updated as .
		 *            necessary.
		 * @return
		 */
		Annotation merge(AST ast, Annotation a, IJavaDeclaration target,
				Set<String> imports);

	}

	@SuppressWarnings("unchecked")
	private Annotation createWrappedAnnotation(final AST ast,
			final String name, final String wrapperName,
			final Set<String> contents, final Set<String> imports) {
		final int len = contents.size();
		if (len == 1) {
			return ann(ast, name, contents.iterator().next(), imports);
		} else if (len > 1) {
			final SingleMemberAnnotation a = ast.newSingleMemberAnnotation();
			a.setTypeName(ast.newName(wrapperName));
			addImport(wrapperName, imports);
			final ArrayInitializer arr = ast.newArrayInitializer();
			final List<Expression> expressions = arr.expressions();
			final List<String> cs = new ArrayList<String>(contents);
			Collections.sort(cs);
			for (final String desc : cs) {
				expressions.add(ann(ast, name, desc, imports));
			}
			a.setValue(arr);
			return a;
		}
		throw new IllegalArgumentException("List cannot be empty");
	}

	@SuppressWarnings("unchecked")
	private Annotation createWrappedAssume(final AST ast,
			final List<Annotation> anns, final Set<String> imports) {
		final int len = anns.size();
		if (len == 1) {
			return anns.get(0);
		} else if (len > 1) {
			final SingleMemberAnnotation a = ast.newSingleMemberAnnotation();
			a.setTypeName(ast.newName(ASSUMES));
			addImport(ASSUMES, imports);
			final ArrayInitializer arr = ast.newArrayInitializer();
			final List<Expression> expressions = arr.expressions();
			for (final Annotation ann : anns) {
				expressions.add(ann);
			}
			a.setValue(arr);
			return a;
		}
		throw new IllegalArgumentException("List cannot be empty");
	}

	/**
	 * Create an annotation matching the given description
	 * 
	 * @param ast
	 * @param desc
	 * @return
	 */

	private Annotation ann(final AST ast, final String name,
			final String contents, final Set<String> imports) {
		addImport(name, imports);
		if (contents != null) {
			final SingleMemberAnnotation ann = ast.newSingleMemberAnnotation();
			ann.setTypeName(ast.newName(name));
			final StringLiteral lit = ast.newStringLiteral();
			lit.setLiteralValue(contents);
			ann.setValue(lit);
			return ann;
		} else {
			final MarkerAnnotation ann = ast.newMarkerAnnotation();
			ann.setTypeName(ast.newName(name));
			return ann;
		}
	}

	static void addImport(final String promise, final Set<String> imports) {
		imports.add(PROMISE_PKG+'.' + promise);
	}

	/**
	 * Returns the value of an annotation, or null if no value exists
	 * 
	 * @param a
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Expression extractValue(final Annotation a) {
		if (a.isNormalAnnotation()) {
			final NormalAnnotation na = (NormalAnnotation) a;
			final List<MemberValuePair> ps = na.values();
			for (final MemberValuePair p : ps) {
				if (p.getName().getIdentifier().equals(AnnotationConstants.VALUE_ATTR)) {
					return p.getValue();
				}
			}
		} else if (a.isSingleMemberAnnotation()) {
			final SingleMemberAnnotation sa = (SingleMemberAnnotation) a;
			return sa.getValue();
		}
		return null;
	}

	private static String join(final Iterable<String> names, final char delim) {
		final StringBuilder b = new StringBuilder();
		for (final String name : names) {
			b.append(name);
			b.append(delim);
		}
		if (b.length() == 0) {
			return "";
		}
		return b.substring(0, b.length() - 1);
	}

	/*
	 * join together a list of names with the ',' separator
	 */
	private static String join(final Iterable<String> names) {
		return join(names, ',');
	}

}
