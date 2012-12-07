package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

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
@Immutable
@ValueObject
public interface IDeclType extends IDecl {
  // marker interface
}
