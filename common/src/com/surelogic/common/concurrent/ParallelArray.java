package com.surelogic.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

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

  public boolean add(E e) {
    return f_elements.add(e);
  }

  public boolean addAll(Collection<? extends E> c) {
    return f_elements.addAll(c);
  }

  /**
   * Returns the number of elements in this list.
   * 
   * @return the number of elements in this list
   */
  public int size() {
    return f_elements.size();
  }

  /**
   * Returns {@code true} if this list contains no elements.
   * 
   * @return {@code true} if this list contains no elements.
   */
  public boolean isEmpty() {
    return f_elements.isEmpty();
  }

  /**
   * Removes all of the elements from this list. The list will be empty after
   * this call returns
   */
  public void clear() {
    f_elements.clear();
  }

  public List<E> asList() {
    return f_elements;
  }

  /**
   * Applies the given procedure to elements using {@code nThreads} threads.
   * This call blocks until all elements are processed.
   * <p>
   * An {@code ExecutorService} is used to run the procedure on each element.
   * The executor is constructed and shutdown within this method.
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
    boolean doneWaiting = false;
    InterruptedException ifInterrupted = null;
    while (true) {
      try {
        doneWaiting = s.awaitTermination(5, TimeUnit.SECONDS);
        if (doneWaiting)
          break;
      } catch (InterruptedException e) {
        doneWaiting = false;
        ifInterrupted = e;
      }
      final String duration = SLUtility.toStringDurationMS(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      if (ifInterrupted == null) {
        SLLogger.getLogger().log(Level.INFO, I18N.err(350, duration));
      } else {
        SLLogger.getLogger().log(Level.INFO, I18N.err(350, duration), ifInterrupted);
        ifInterrupted = null;
      }
    }
  }

  /**
   * Applies the given procedure to elements using
   * {@code Runtime.getRuntime().availableProcessors()} threads. This call
   * blocks until all elements are processed.
   * <p>
   * An {@code ExecutorService} is used to run the procedure on each element.
   * The executor is constructed and shutdown within this method.
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
