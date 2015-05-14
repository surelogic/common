package com.surelogic.common.jobs.remote;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

/**
 * JSure JVMs connect to Eclipse to give progress and debug info
 * 
 * @author Edwin
 */
public class Console extends Thread {
  public Console(String name, IClientHandlerFactory factory, int port) {
    super(name);
    f_handlerFactory = factory;
    f_port = port;
  }

  private volatile boolean f_shutdownRequested = false;

  private final int f_port;

  private final IClientHandlerFactory f_handlerFactory;

  /**
   * The socket this is listening on.
   */
  private ServerSocket f_socket;

  /**
   * Maintains a list of the client handlers. {@link WeakReference}s are used to
   * allow dead threads to be garbage collected. Note that we hang onto all the
   * weak references until the program ends (this could probably be improved
   * with a reference queue...but we don't expects a lot of connections).
   */
  private final List<WeakReference<IClientHandler>> f_handlers = new ArrayList<>();

  public void logAProblem(String msg, Throwable e) {
    SLLogger.getLogger().log(Level.WARNING, msg, e);
  }

  public void logAProblem(String msg) {
    SLLogger.getLogger().warning(msg);
  }

  public void log(String msg) {
    SLLogger.getLogger().info(msg);
  }

  public int getPort() {
    if (f_socket == null) {
      return -1;
    }
    return f_socket.getLocalPort();
  }

  @Override
  public void run() {
    // start listening on a port
    boolean listening = false;
    int tryCount = 0;
    int port = f_port;
    do {
      try {
        port = f_port + tryCount;
        f_socket = new ServerSocket(port);
        listening = true;
      } catch (IOException e) {
        tryCount++;
      }
    } while (!listening && tryCount <= 100);
    if (!listening) {
      logAProblem("unable to listen on any port between " + f_port + " and " + port
          + " (i.e., this cannot be shutdown via a console)");
      return;
    }
    log("console server listening on port " + port);

    // until told to shutdown, listen for and handle client connections
    while (!f_shutdownRequested) {
      try {
        final Socket client = f_socket.accept(); // wait for a client
        InetAddress address = client.getInetAddress();
        final IClientHandler handler = f_handlerFactory.newHandler(this, client);
        log("console connect from " + (address == null ? "UNKNOWN" : address.getCanonicalHostName()) + " (" + handler.getName()
            + ")");
        final WeakReference<IClientHandler> p_handler = new WeakReference<>(handler);
        f_handlers.add(p_handler);
        handler.start();
      } catch (SocketException e) {
        /*
         * ignore, this is normal behavior during a shutdown, i.e., another
         * thread has called gameSocket.close() via our requestShutdown()
         * method.
         */
      } catch (IOException e) {
        logAProblem("failure listening for client connections " + port, e);
      }
    }
    /*
     * Shutdown all the client handler threads. By doing this here, and not
     * within the shutdown() method, we can keep the f_handlers list
     * thread-local.
     */
    for (WeakReference<IClientHandler> p_handler : f_handlers) {
      final IClientHandler handler = p_handler.get();
      if (handler != null) {
        handler.requestShutdown();
      }
    }
  }

  /**
   * Signals that this console and all open client handler threads should be
   * shutdown. This method returns immediately.
   */
  void requestShutdown() {
    f_shutdownRequested = true;
    try {
      if (f_socket != null) {
        f_socket.close();
      }
    } catch (IOException e) {
      logAProblem("unable to close the socket used by " + getName(), e);
    }
  }

  static AtomicInteger f_instanceCount = new AtomicInteger();
}
