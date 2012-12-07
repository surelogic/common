package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

/**
 * Interface for declaration instances where {@link #getKind()} ==
 * {@link Kind#PACKAGE}.
 */
@Immutable
@ValueObject
public interface IDeclPackage extends IDecl {
  // marker interface
}
