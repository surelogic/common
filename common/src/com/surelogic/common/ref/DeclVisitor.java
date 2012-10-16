package com.surelogic.common.ref;

import java.util.List;

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
  public void visitPackage(IDecl node) {
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
  public boolean visitTypes(List<IDecl> types) {
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
   * @return {@code true} if the type parameters of this class should be
   *         visited, {@code false} otherwise.
   */
  public boolean visitClass(IDecl node) {
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
   *         visited, {@code false} otherwise.
   */
  public boolean visitInterface(IDecl node) {
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
  public void visitEnum(IDecl node) {
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
  public void visitField(IDecl node) {
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
   *         should be visited, {@code false} otherwise.
   */
  public boolean visitMethod(IDecl node) {
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
   *         constructor should be visited, {@code false} otherwise.
   */
  public boolean visitConstructor(IDecl node) {
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
  public boolean visitParameters(List<IDecl> parameters) {
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
   */
  public void visitParameter(IDecl node) {
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
  public boolean visitTypeParameters(List<IDecl> typeParameters) {
    return true;
  }

  /**
   * Visits a type parameter declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#TYPE_PARAMETER}</tt> .
   */
  public void visitTypeParameter(IDecl node) {
    // by default do nothing
  }

  /**
   * End of the visit for a package declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#PACKAGE}</tt>.
   */
  public void endVisitPackage(IDecl node) {
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
  public void endVisitTypes(List<IDecl> types) {
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
  public void endVisitClass(IDecl node) {
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
  public void endVisitInterface(IDecl node) {
    // by default do nothing
  }

  /**
   * End of the visit for an enum declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#ENUM}</tt>.
   */
  public void endVisitEnum(IDecl node) {
    // by default do nothing
  }

  /**
   * End of the visit for a field declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#FIELD}</tt> .
   */
  public void endVisitField(IDecl node) {
    // by default do nothing
  }

  /**
   * End of the visit for an initializer declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#INITIALIZER}</tt> .
   */
  public void endVisitInitializer(IDecl node) {
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
  public void endVisitMethod(IDecl node) {
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
  public void endVisitConstructor(IDecl node) {
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
  public void endVisitParameters(List<IDecl> parameters) {
    // by default do nothing
  }

  /**
   * End of the visit for a parameter declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#PARAMETER}</tt>.
   */
  public void endVisitParameter(IDecl node) {
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
  public void endVisitTypeParameters(List<IDecl> typeParameters) {
    // by default do nothing
  }

  /**
   * End of the visit for a type parameter declaration.
   * <p>
   * The default implementation does nothing. Subclasses may reimplement.
   * 
   * @param node
   *          the node to visit where <tt>node.getKind() ==
   *          {@link IDecl.Kind#TYPE_PARAMETER}</tt> .
   */
  public void endVisitTypeParameter(IDecl node) {
    // by default do nothing
  }
}
