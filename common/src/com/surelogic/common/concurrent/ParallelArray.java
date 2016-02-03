package com.surelogic.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.surelogic.common.SLUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * This class allows parallel tasks to be run on an encapsulated list of
 * elements. Tasks are passed via a {@link Procedure} to one of the
 * {@code apply} methods.
 * <p>
 * <i>ParallelArray</i> is a bad name for this class: it isn't an array, it uses
 * a collection under the hood. However, in spirit it functions in a similar
 * manner to Doug Lea's {@code ParallelArray} on
 * <a href="http://g.oswego.edu/dl/concurrency-interest/">Doug Lea's
 * concurrency-interest web page</a> in the package extra166y.
 * <p>
 * All operations on the encapsulated list are done via {@link #asList()}. For
 * example to add an element, {@code elt}, to the {@link ParallelArray}
 * {@code pa} you write {@code pa.asList().add(elt)}. {@link #asList()} returns
 * a reference to the encapsulated list that can be mutated freely.
 * <p>
 * The encapsulated list is a thread safe collection.
 *
 * @param <E>
 *          the type to run a task on in parallel.
 */
public final class ParallelArray<E> {

  private final ArrayList<E> f_elements;

  public ParallelArray() {
    f_elements = new ArrayList<>();
  }

  public ParallelArray(int initialCapacity) {
    f_elements = new ArrayList<>(initialCapacity);
  }

  public ParallelArray(Collection<? extends E> c) {
    f_elements = new ArrayList<>(c);
  }

  /**
   * Returns a reference to the encapsulated list for queries and mutations. Any
   * changes are see by this.
   * 
   * @return a mutable list encapsulated by this.
   */
  public List<E> asList() {
    return f_elements;
  }

  /**
   * Applies the given procedure to elements using {@code nThreads} threads.
   * This call blocks until all elements are processed.
   * <p>
   * An {@code ExecutorService} is used to run the procedure on each element.
   * The executor is constructed and shutdown within this method.
   * <p>
   * Information is output to the log every 5 seconds, or any time the blocked
   * thread is interrupted, while this method waits for the encapsulated
   * {@code ExecutorService} to shutdown (presumably it is finishing up its
   * tasks).
   * 
   * @param procedure
   *          the procedure
   * @param nThreads
   *          the number of threads in the pool (e.g.,
   *          {@code Executors.newFixedThreadPool(nThreads)})
   * @throws IllegalArgumentException
   *           if {@code nThreads <= 0} or if {@code procedure} is {@code null}.
   */
  public void apply(final Procedure<? super E> procedure, int nThreads) {
    if (procedure == null)
      throw new IllegalArgumentException(I18N.err(44, "procedure"));
    long start = System.nanoTime();
    final ExecutorService s = Executors.newFixedThreadPool(nThreads);
    for (final E e : f_elements)
      s.submit(new Runnable() {
        public void run() {
          procedure.op(e);
        }
      });
    s.shutdown();
    InterruptedException ifInterrupted = null;
    while (true) {
      try {
        if (s.awaitTermination(5, TimeUnit.SECONDS))
          break;
      } catch (InterruptedException e) {
        ifInterrupted = e;
      }
      final String duration = SLUtility.toStringDurationMS(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      if (ifInterrupted == null) {
    	if (!XUtil.runJSureInMemory) {
    	  SLLogger.getLogger().log(Level.INFO, I18N.err(350, duration));
    	}
      } else {
        SLLogger.getLogger().log(Level.INFO, I18N.err(350, duration), ifInterrupted);
        ifInterrupted = null;
      }
    }
    // output how may elements were process and how long it took (fine logging
    // only)
    if (SLLogger.getLogger().isLoggable(Level.FINE)) {
      final String duration = SLUtility.toStringDurationMS(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      SLLogger.getLogger().log(Level.FINE, I18N.err(351, f_elements.size(), duration));
    }
  }

  /**
   * Applies the given procedure to elements using
   * {@code Runtime.getRuntime().availableProcessors()} threads. This call
   * blocks until all elements are processed.
   * <p>
   * An {@code ExecutorService} is used to run the procedure on each element.
   * The executor is constructed and shutdown within this method. In particular
   * this should kill all the thread and clear out any thread-local data that
   * was setup by the procedure.
   * <p>
   * Information is output to the log every 5 seconds, or any time the blocked
   * thread is interrupted, while this method waits for the encapsulated
   * {@code ExecutorService} to shutdown (presumably it is finishing up its
   * tasks).
   * 
   * @param procedure
   *          the procedure
   * @throws IllegalArgumentException
   *           if {@code nThreads <= 0}
   */
  public void apply(Procedure<? super E> procedure) {
    apply(procedure, Runtime.getRuntime().availableProcessors());
  }
}
