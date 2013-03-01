package com.surelogic.common.adhoc;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;

import com.surelogic.Nullable;
import com.surelogic.ReferenceObject;
import com.surelogic.common.CommonImages;
import com.surelogic.common.jdbc.DBConnection;

/**
 * Implemented by data providers for an ad hoc query capability. Typically this
 * interface will be implemented in another plug-in or module that wants to
 * reuse the ad hoc query capability implemented in this plug-in/module.
 */
@ReferenceObject
public interface IAdHocDataSource {

  /**
   * Gets a handle to the file where the set of queries should be loaded from
   * and saved to. This method should not return {@code null}.
   * 
   * @return a non-null file.
   */
  File getQuerySaveFile();

  /**
   * Gets the URL to a default query save file to use if the file handle
   * returned by {@link #getQuerySaveFile()} does not reference a file that
   * exists.
   * 
   * @return the URL to a default query save file or {@code null} if none.
   */
  URL getDefaultQueryUrl();

  /**
   * Invoked if the file returned by {@link #getQuerySaveFile()} is not able to
   * be read from or written to for any reason. This could indicate a
   * permissions problem or a file in the wrong format.
   * 
   * @param e
   *          the exception that occurred while attempting to read or write the
   *          query save file.
   */
  void badQuerySaveFileNotification(Exception e);

  /**
   * Gets a connection to the database that ad hoc queries are to be run on.
   * 
   * @return a non-null connection to the database.
   * @throws SQLException
   *           if interaction with the database fails.
   */
  DBConnection getDB();

  /**
   * Returns the maximum number of rows displayed per query. Any result set
   * returned from the database will be truncated to this number.
   * 
   * @return the maximum number of rows displayed per query.
   */
  int getMaxRowsPerQuery();

  /**
   * This method returns the identifier of the query editor view associated with
   * this data source. This identifier is used to hyperlink into the view.
   * Implementation is optional and a return of {@code null} is allowed to
   * indicate that hyperlinking to the editor view should not be supported.
   * 
   * @return the identifier of the query editor view, or {@code null} if
   *         hyperlinking to this view is not supported.
   */
  String getEditorViewId();

  /**
   * This method returns the access key that should be held by all jobs that use
   * this data source. The access key is used by the job system to ensure that
   * UI database jobs operate in a serial fashion.
   * 
   * @return a non-<code>null</code> String
   */
  String[] getCurrentAccessKeys();

  /**
   * Indicates that a particular query on the database will result in no data.
   * Intended to be overridden by subclasses to provide an answer.
   * <p>
   * This method is used to provide visual indication in the user interface that
   * the query will not result in data.
   * <p>
   * If the answer is not known an implementation should return {@code false}.
   * 
   * @param query
   *          the query.
   * @return {@code true} if the query when run on the database is known to
   *         result in no data, {@code false} otherwise.
   */
  boolean queryResultWillBeEmpty(AdHocQuery query);

  /**
   * Finds the passed class and returns it if it is of type
   * <tt>IQueryResultCustomDisplay</tt>.
   * <p>
   * Implementations should always return <tt>IQueryResultCustomDisplay</tt> not
   * {@link Object}.
   * 
   * @param className
   *          name of the class to find on the classpath.
   * @return an instance of the class.
   * 
   * @throws UnsupportedOperationException
   *           if custom views are not supported by this data source.
   * @throws Exception
   *           if the class is not found, or something else goes wrong, such as
   *           the type is not <tt>IQueryResultCustomDisplay</tt>.
   */
  Object getCustomDisplay(String className) throws Exception;

  /**
   * Gets a reference to the passed image for use in Querydoc. The passed
   * reference will be copied into a temporary file and then used by the
   * Querydoc. Therefore, the returned reference can be within a Jar.
   * <p>
   * As a fallback if a name can't be found, and rather than not providing an
   * implementation, use
   * 
   * <pre>
   * return CommonImages.getImageURL(imageName);
   * </pre>
   * 
   * to allow Querydoc to use the symbolic images names in {@link CommonImages}.
   * 
   * @param imageName
   *          an image name understood by the implementation.
   * @return a reference to an image file.
   */
  @Nullable
  URL getQuerydocImageURL(String imageName);
}
