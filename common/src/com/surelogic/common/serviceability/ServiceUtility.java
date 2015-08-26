package com.surelogic.common.serviceability;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import com.surelogic.Utility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

@Utility
public final class ServiceUtility {
  /*
   * URL at the SureLogic website to send serviceability requests
   */
  final static String f_serviceLocation = I18N.msg("web.supportrequest.url", SLUtility.SERVICEABILITY_URL);

  /**
   * Constructs a job that sends a message over the Internet to SureLogic.
   * 
   * @param msg
   *          the message for SureLogic.
   */
  public static SLJob sendToSureLogic(final String msg) {
    return sendToSureLogic(msg, null);
  }

  public static SLJob sendToSureLogic(final String msg, final Runnable after) {
    return new AbstractSLJob("Sending a servicability message to SureLogic") {

      @Override
      public SLStatus run(final SLProgressMonitor monitor) {
        monitor.begin();
        try {
          // Prepare the URL connection
          final URL url = new URL(f_serviceLocation);
          final URLConnection conn = url.openConnection();
          conn.setDoInput(true);
          conn.setDoOutput(true);
          conn.setUseCaches(false);

          final OutputStream os = null;
          OutputStreamWriter wr = null;
          try {
            // Send the request
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(msg);
            wr.flush();

            // Check the response
            final InputStream is = conn.getInputStream();
            is.close();

            if (after != null) {
              after.run();
            }
          } finally {
            if (wr != null) {
              wr.close();
            } else if (os != null) {
              os.close();
            }
          }
        } catch (final Exception e) {
          return SLStatus.createErrorStatus(I18N.err(154, f_serviceLocation), e);
        }

        monitor.done();
        return SLStatus.OK_STATUS;
      }
    };
  }

  private ServiceUtility() {
    // no instances
  }
}
