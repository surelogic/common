package com.surelogic.common.ref;

import com.surelogic.common.ref.IDecl.Kind;
import com.surelogic.Immutable;

/**
 * Interface for declaration instances where {@link #getKind()} is one of the
 * following:
 * <ul>
 * <li>{@link Kind#CONSTRUCTOR}</li>
 * <li>{@link Kind#METHOD}</li>
 * </ul>
 */
@Immutable
public interface IDeclFunction extends IDecl {
  // marker interface
}
