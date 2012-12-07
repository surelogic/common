package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

/**
 * Interface for declaration instances where {@link #getKind()} ==
 * {@link Kind#FIELD}.
 */
@Immutable
@ValueObject
public interface IDeclField extends IDecl {
  // marker interface
}
