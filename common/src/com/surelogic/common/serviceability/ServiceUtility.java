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
  final static String f_serviceLocation = I18N.msg("web.support.url", SLUtility.SERVICEABILITY_SERVER);

  /**
   * Constructs a job that sends a message over the Internet to SureLogic.
   * 
   * @param message
   *          the message for SureLogic.
   */
  public static SLJob sendToSureLogic(final String message) {
    return sendToSureLogic(message, null);
  }

  public static SLJob sendToSureLogic(String message, final Runnable after) {
    final char[] msg = message.toCharArray();
    return new AbstractSLJob("Sending to SureLogic...please be patient this may take a minute or two") {

      @Override
      public SLStatus run(final SLProgressMonitor monitor) {
        final int endWork = Math.max(20, msg.length / 20);
        monitor.begin(msg.length + (2 * endWork));
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
            int off = 0;
            final int blk = 512; // characters
            while (off < msg.length) {
              final int len = Math.min(blk, msg.length - off);
              wr.write(msg, off, len);
              monitor.worked(len);
              off += blk;
            }
            wr.flush();

            // Check the response
            final InputStream is = conn.getInputStream();
            is.close();
            monitor.worked(endWork);

            if (after != null) {
              after.run();
            }
            monitor.worked(endWork);
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
