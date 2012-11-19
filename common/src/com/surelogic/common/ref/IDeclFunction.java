package com.surelogic.common.ref;

import com.surelogic.common.ref.IDecl.Kind;

/**
 * Interface for declaration instances where {@link #getKind()} is one of the
 * following:
 * <ul>
 * <li>{@link Kind#CONSTRUCTOR}</li>
 * <li>{@link Kind#METHOD}</li>
 * </ul>
 */
public interface IDeclFunction extends IDecl {
  // marker interface
}
