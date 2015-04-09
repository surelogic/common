package com.surelogic.server.serviceability;

public interface BugzillaConstants {
  public static final String ID = "id";

  // Required - The name of the product the bug is being filed against.
  public static final String PRODUCT = "product";

  // Required - The name of a component in the product above.
  public static final String COMPONENT = "component";

  // Required - A brief description of the bug being filed.
  public static final String SUMMARY = "summary";

  // Required - A version of the product above; the version the bug was found
  // in.
  public static final String VERSION = "version";

  // Defaulted - The initial description for this bug. Some Bugzilla
  // installations require this to not be blank.
  public static final String DESCRIPTION = "description";
}
