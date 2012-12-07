package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

/**
 * Interface for declaration instances where {@link #getKind()} ==
 * {@link Kind#PARAMETER}.
 */
@Immutable
@ValueObject
public interface IDeclParameter extends IDecl {
  // marker interface
}
