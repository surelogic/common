package com.surelogic.common.jdbc;

import java.net.URL;

/**
 * Allows access to schema and version information for a particular tool.
 * <p>
 * <i>Implementation Note:</i> This interface is required because the Eclipse
 * class loader requires that implementors be within the same plug-in as the
 * schema resources. (We put them in the same package as the resources.)
 */
public interface SchemaData {

  /**
   * Gets the version that the database schema should be at to be consistent
   * with the code.
   * <p>
   * It is recommended that this be read from a file in the same package as the
   * schema definition files named <tt>version.txt</tt>.
   * 
   * @return the version that the database schema should be at to be consistent
   *         with the code.
   */
  public int getVersion();

  /**
   * Gets a reference to a named resource within the tool. For example,
   * <tt>derby_0000.sql</tt> could be requested and the tool would look for this
   * resource within its tool specific package.
   * 
   * @param name
   *          the name to search for.
   * @return the schema resource, or {@code null} if it doesn't exist.
   */
  public URL getSchemaResource(String name);

  /**
   * Gets a reference to a named schema action within the tool. For example,
   * <tt>Server_0001</tt> would construct an instance of that class within the
   * tool specific package and return a reference to that instance.
   * 
   * @param action
   *          the name of the action to search for.
   * @return the schema action, or {@code null} if it doesn't exist.
   * @throws IllegalStateException
   *           if an illegal access or an instantiation problem occurs.
   */
  public SchemaAction getSchemaAction(String action);
}
