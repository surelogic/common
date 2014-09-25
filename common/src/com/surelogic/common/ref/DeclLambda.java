package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;

@Immutable
final class DeclLambda extends Decl implements IDeclLambda {

  final int f_declPosition;
  @Nullable
  final TypeRef f_returnTypeOf;
  @NonNull
  final TypeRef f_functionalInterfaceTypeOf;

  DeclLambda(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, int declPosition, TypeRef returnTypeOf,
      TypeRef functionalInterfaceTypeOf) {
    super(parent, childBuilders, name);
    f_declPosition = declPosition;
    f_returnTypeOf = returnTypeOf;
    f_functionalInterfaceTypeOf = functionalInterfaceTypeOf;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.LAMBDA;
  }

  @Override
  public int getPosition() {
    return f_declPosition;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_returnTypeOf;
  }

  @Override
  public TypeRef getLambdaFunctionalInterfaceTypeOf() {
    return f_functionalInterfaceTypeOf;
  }
}
