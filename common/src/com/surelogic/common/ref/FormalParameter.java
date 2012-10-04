package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ref.IDecl.Kind;

/**
 * Represents a formal parameter of a method or a constructor declarations.
 * 
 * @see IDecl
 */
public final class FormalParameter {

  public static final FormalParameter[] EMPTY = new FormalParameter[0];

  /**
   * Constructs a formal parameter instance.
   * 
   * @param name
   *          the formal parameter name.
   * @param ofType
   *          a type. A <tt>class</tt>, <tt>enum</tt>, or <tt>interface</tt>.
   * @return a formal parameter.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  public static FormalParameter getInstance(String name, IDecl ofType) {
    return new FormalParameter(name, ofType);
  }

  /**
   * Constructs a formal parameter instance. The formal parameter name will be
   * <tt>arg</tt><i>n</i>, where <i>n</i> is the number, starting at zero,
   * indicating its order of occurrence in the list of parameters&mdash;<i>n</i>
   * is passed as <tt>argNum</tt>.
   * 
   * @param argNum
   *          the argument number.
   * @param ofType
   *          a type. A <tt>class</tt>, <tt>enum</tt>, or <tt>interface</tt>.
   * @return a formal parameter.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  public static FormalParameter getInstanceByArgNum(int argNum, IDecl ofType) {
    final String name = "arg" + argNum;
    return new FormalParameter(name, ofType);
  }

  @NonNull
  private final IDecl f_ofType;
  @NonNull
  private final String f_name;

  private FormalParameter(String formalName, IDecl ofType) {
    if (formalName == null)
      throw new IllegalArgumentException(I18N.err(44, "formalName"));
    f_name = formalName;
    if (ofType == null)
      throw new IllegalArgumentException(I18N.err(44, "ofType"));
    if (!(ofType.getKind() == Kind.CLASS || ofType.getKind() == Kind.INTERFACE || ofType.getKind() == Kind.ENUM))
      throw new IllegalArgumentException(I18N.err(268, formalName, ofType));
    f_ofType = ofType;
  }

  @NonNull
  public IDecl getType() {
    return f_ofType;
  }

  @NonNull
  public String getName() {
    return f_name;
  }

  @Override
  public String toString() {
    return "FormalParameter(" + f_name + ":" + f_ofType + ")";
  }
}
