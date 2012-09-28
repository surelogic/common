package com.surelogic.common;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

/**
 * Provides a partial implementation of this class that should be used by other
 * implementations to get the details of the {@link IJavaRef} correct.
 * 
 * @author Tim
 * 
 */
@ThreadSafe
public abstract class AbstractJavaRef implements IJavaRef {
  @NonNull
  private final Within f_within;
  @NonNull
  private final String f_relativePath;
  @Nullable
  private final String f_eclipseProject;

  protected AbstractJavaRef(final @NonNull Within within, final @NonNull String relativePath,
      final @Nullable String eclipseProjectNameOrNull) {
    if (within == null)
      throw new IllegalArgumentException(I18N.err(44, "within"));
    f_within = within;
    if (relativePath == null)
      throw new IllegalArgumentException(I18N.err(44, "relativePath"));
    f_relativePath = relativePath;
    f_eclipseProject = eclipseProjectNameOrNull;
  }

  @NonNull
  public final Within getWithin() {
    return f_within;
  }

  public final boolean isFromSource() {
    return f_within == IJavaRef.Within.JAVA_FILE;
  }

  @Override
  @NonNull
  public String getRelativePath() {
    return f_relativePath;
  }

  @Override
  @NonNull
  public String getFileName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getLineNumber() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  @NonNull
  public String getEclipseProjectName() {
    if (f_eclipseProject == null)
      return SLUtility.UNKNOWN_PROJECT;
    else
      return f_eclipseProject;
  }

  @Override
  @NonNull
  public String getPackageName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public TypeType getTypeType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeNameFullyQualified() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeNameFullyQualifiedJarStyle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getJavaId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getEnclosingJavaId() {
    // TODO Auto-generated method stub
    return null;
  }
}
