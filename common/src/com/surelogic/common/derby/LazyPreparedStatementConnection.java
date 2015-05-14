package com.surelogic.common.derby;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.derby.impl.jdbc.EmbedConnection;

import com.surelogic.common.jdbc.CancellableConnection;

/**
 * LazyPreparedStatementConnection proxies Connection and supplies slightly
 * different (but still valid) behavior than a normal Connection. Essentially, a
 * PreparedStatement is not actually created until someone attempts to invoke a
 * method on it. In addition, all PreparedStatement objects that have not
 * already been closed are closed when a Connection is closed.
 */
public final class LazyPreparedStatementConnection implements InvocationHandler {

  final Connection conn;
  final Set<PreparedStatement> statements;

  public LazyPreparedStatementConnection(final Connection conn) {
    this.conn = conn;
    statements = new HashSet<>();
  }

  public static CancellableConnection wrap(final Connection conn) {
    return (CancellableConnection) Proxy.newProxyInstance(CancellableConnection.class.getClassLoader(),
        new Class[] { CancellableConnection.class }, new LazyPreparedStatementConnection(conn));
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if ("prepareStatement".equals(method.getName())) {
      return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class[] { PreparedStatement.class },
          new LazyPreparedStatement(method, args));
    } else if ("close".equals(method.getName())) {
      for (final PreparedStatement st : statements) {
        st.close();
      }
    } else if ("cancelRunningStatement".equals(method.getName())) {
      ((EmbedConnection) conn).cancelRunningStatement();
      return null;
    }
    try {
      return method.invoke(conn, args);
    } catch (final InvocationTargetException e) {
      final Throwable target = e.getTargetException();
      if (target instanceof Exception) {
        throw (Exception) target;
      } else {
        throw e;
      }
    }
  }

  private class LazyPreparedStatement implements InvocationHandler {

    private final Callable<PreparedStatement> init;
    private PreparedStatement st;

    public LazyPreparedStatement(final Method method, final Object... args) {
      init = new Callable<PreparedStatement>() {

        @Override
        public PreparedStatement call() throws Exception {
          try {
            final PreparedStatement st = (PreparedStatement) method.invoke(conn, args);
            statements.add(st);
            return st;
          } catch (final InvocationTargetException e) {
            final Throwable target = e.getTargetException();
            if (target instanceof Exception) {
              throw (Exception) target;
            } else {
              throw e;
            }
          }
        }

      };
    }

    void check() throws Exception {
      if (st == null) {
        st = init.call();
      }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
      check();
      try {
        final Object val = method.invoke(st, args);
        if ("close".equals(method.getName())) {
          statements.remove(st);
        }
        return val;
      } catch (final InvocationTargetException e) {
        final Throwable target = e.getTargetException();
        if (target instanceof Exception) {
          throw (Exception) target;
        } else {
          throw e;
        }
      }
    }

  }
}
