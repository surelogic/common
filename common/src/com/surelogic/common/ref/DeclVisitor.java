package com.surelogic.common.ref;

import java.util.List;

/**
 * A base visitor for declarations. Subtypes are intended to define useful
 * behavior.
 * 
 * @see IDecl#acceptRootToThis(DeclVisitor)
 * @see IDecl#acceptThisToRoot(DeclVisitor)
 */
public abstract class DeclVisitor {

  /**
   * Visits the passed declaration prior to the type-specific visit.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit.
   */
  public void preVisit(IDecl node) {
    // by default do nothing
  }

  /**
   * Visits the passed declaration following the type-specific visit.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit.
   */
  public void postVisit(IDecl node) {
    // by default do nothing
  }

  /**
   * Visits a package declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#PACKAGE}</tt>.
   */
  public void visitPackage(IDeclPackage node) {
    // by default do nothing
  }

  /**
   * Visits a possibly nested type declaration, prior to visiting each type
   * declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param types
   *          a non-empty list of types, ordered innermost to outermost. The
   *          kind of all elements should be contained in {@link IDecl#IS_TYPE}.
   * @return {@code true} if the individual types passed in <tt>types</tt>
   *         should be visited, {@code false} otherwise.
   */
  public boolean visitTypes(List<IDeclType> types) {
    return true;
  }

  /**
   * Visits a class declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#CLASS}</tt>.
   * @return {@code true} if the type parameters of this class should be visited
   *         (this includes calling {@link #visitTypeParameters(List)}),
   *         {@code false} otherwise.
   */
  public boolean visitClass(IDeclType node) {
    return true;
  }

  /**
   * Visits a interface declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#INTERFACE}</tt>.
   * @return {@code true} if the type parameters of this interface should be
   *         visited (this includes calling {@link #visitTypeParameters(List)}),
   *         {@code false} otherwise.
   */
  public boolean visitInterface(IDeclType node) {
    return true;
  }

  /**
   * Visits an enum declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#ENUM}</tt>.
   */
  public void visitEnum(IDeclType node) {
    // by default do nothing
  }

  /**
   * Visits a field declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#FIELD}</tt> .
   */
  public void visitField(IDeclField node) {
    // by default do nothing
  }

  /**
   * Visits an initializer declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#INITIALIZER}</tt> .
   */
  public void visitInitializer(IDecl node) {
    // by default do nothing
  }

  /**
   * Visits a method declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#METHOD}</tt>.
   * @return {@code true} if the parameters and type parameters of this method
   *         should be visited (this includes calling
   *         {@link #visitTypeParameters(List)} and then
   *         {@link #visitParameters(List)}), {@code false} otherwise.
   */
  public boolean visitMethod(IDeclFunction node) {
    return true;
  }

  /**
   * Visits a constructor declaration.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#CONSTRUCTOR}</tt>.
   * @return {@code true} if the parameters and type parameters of this
   *         constructor should be visited (this includes calling
   *         {@link #visitTypeParameters(List)} and then
   *         {@link #visitParameters(List)}), {@code false} otherwise.
   */
  public boolean visitConstructor(IDeclFunction node) {
    return true;
  }

  /**
   * Visits a list of parameters, prior to visiting each type parameter.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param parameters
   *          a possibly empty list of parameters, ordered by position. The kind
   *          of all elements should be {@link IDecl.Kind#PARAMETER}.
   * @return {@code true} if the individual parameters passed in
   *         <tt>parameters</tt> should be visited, {@code false} otherwise.
   */
  public boolean visitParameters(List<IDeclParameter> parameters) {
    return true;
  }

  /**
   * Visits a parameter declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#PARAMETER}</tt>.
   * @param partOfDecl
   *          {@code true} if this call is being made because this parameter is
   *          part of the declaration being visited, {@code false} if this call
   *          is being made because the parameter is part of a parameter list
   *          for a method or constructor.
   */
  public void visitParameter(IDeclParameter node, boolean partOfDecl) {
    // by default do nothing
  }

  /**
   * Visits a list of type parameters, prior to visiting each type parameter.
   * <p>
   * The default implementation does nothing but return {@code true}. Subclasses
   * may reimplement.
   * 
   * @param typeParameters
   *          a possibly empty list of type parameters, ordered by position. The
   *          kind of all elements should be {@link IDecl.Kind#PARAMETER}.
   * @return {@code true} if the individual type parameters passed in
   *         <tt>typeParameters</tt> should be visited, {@code false} otherwise.
   */
  public boolean visitTypeParameters(List<IDeclTypeParameter> typeParameters) {
    return true;
  }

  /**
   * Visits a type parameter declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#TYPE_PARAMETER}</tt>.
   * @param partOfDecl
   *          {@code true} if this call is being made because this type
   *          parameter is part of the declaration being visited, {@code false}
   *          if this call is being made because the parameter is part of a type
   *          parameter list for a class, interface, method, or constructor.
   */
  public void visitTypeParameter(IDeclTypeParameter node, boolean partOfDecl) {
    // by default do nothing
  }

  /**
   * End of the visit for a possibly nested type declaration, after visiting
   * each type declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param types
   *          a non-empty list of types, ordered innermost to outermost. The
   *          kind of all elements should be contained in {@link IDecl#IS_TYPE}.
   */
  public void endVisitTypes(List<IDeclType> types) {
    // by default do nothing
  }

  /**
   * End of the visit for a class declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#CLASS}</tt>.
   */
  public void endVisitClass(IDeclType node) {
    // by default do nothing
  }

  /**
   * End of the visit for a interface declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#INTERFACE}</tt>.
   */
  public void endVisitInterface(IDeclType node) {
    // by default do nothing
  }

  /**
   * End of the visit for a method declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#METHOD}</tt>.
   */
  public void endVisitMethod(IDeclFunction node) {
    // by default do nothing
  }

  /**
   * End of the visit for a constructor declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#CONSTRUCTOR}</tt>.
   */
  public void endVisitConstructor(IDeclFunction node) {
    // by default do nothing
  }

  /**
   * End of the visit for a list of parameters, after visiting each type
   * parameter.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param parameters
   *          a possibly empty list of parameters, ordered by position. The kind
   *          of all elements should be {@link IDecl.Kind#PARAMETER}.
   */
  public void endVisitParameters(List<IDeclParameter> parameters) {
    // by default do nothing
  }

  /**
   * End of the visit for a list of type parameters, after visiting each type
   * parameter.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param typeParameters
   *          a possibly empty list of type parameters, ordered by position. The
   *          kind of all elements should be {@link IDecl.Kind#PARAMETER}.
   */
  public void endVisitTypeParameters(List<IDeclTypeParameter> typeParameters) {
    // by default do nothing
  }
}
