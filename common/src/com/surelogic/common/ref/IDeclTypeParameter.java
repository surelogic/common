package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

/**
 * Interface for declaration instances where {@link #getKind()} ==
 * {@link Kind#TYPE_PARAMETER}.
 */
@Immutable
@ValueObject
public interface IDeclTypeParameter extends IDecl {
  // marker interface
}
