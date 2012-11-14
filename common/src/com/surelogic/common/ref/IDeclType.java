package com.surelogic.common.ref;

/**
 * Interface for declaration instances where {@link #getKind()} is one of the
 * following:
 * <ul>
 * <li>{@link Kind#ANNOTATION}</li>
 * <li>{@link Kind#CLASS}</li>
 * <li>{@link Kind#ENUM}</li>
 * <li>{@link Kind#INTERFACE}</li>
 * </ul>
 */
public interface IDeclType extends IDecl {
  // marker interface
}
