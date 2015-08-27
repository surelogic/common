package com.surelogic.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.server.serviceability.LicenseRequestServlet;

public final class SiteUtil {

  static final Logger LOG = SLLogger.getLoggerFor(LicenseRequestServlet.class);

  static public final PrivateKey getKey() {
    try {
      Class<?> pkUtility = Class.forName("com.surelogic.key.SLPrivateKeyUtility");
      Method getKey = pkUtility.getMethod("getKey", new Class<?>[0]);
      return (PrivateKey) getKey.invoke(pkUtility, new Object[] {});
    } catch (ClassNotFoundException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    } catch (SecurityException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    } catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    } catch (InvocationTargetException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    throw new IllegalStateException("The server failed in an unexpected fashion. Check server logs.");
  }

  public static final AtomicReference<String> bar = new AtomicReference<>(null);

  public static String getAdminNavBar() {
    String result = bar.get();
    if (result == null) {
      final StringBuilder b = new StringBuilder();
      b.append("( ");
      b.append("<a href=\"home\">To License Overview</a>");
      b.append(" | ");
      b.append("<a href=\"log\">To Recent License Activity</a>");
      b.append(" | ");
      b.append("<a href=\"blacklist\">To Blacklist</a>");
      b.append(" | ");
      b.append("<a href=\"search\">To License Search</a>");
      b.append(" | ");
      b.append("<a href=\"weblog\">To Recent Web License Request Activity</a>");
      b.append(" | ");
      b.append("<a href=\"websearch\">To Web License Request Search</a>");
      b.append(" )");
      result = b.toString();
    }
    return result;
  }

}
