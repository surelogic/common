package com.surelogic.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.surelogic.*;

/**
 * Class that uses a common buffer between and {@link OutputStream} and an
 * {@link InputStream}. The idea is that first the data is written, and then
 * read. Specially, after creation or after being {@link #reset() reset}, the
 * object is in the READY state. The object can then be used as follows:
 * <ol>
 * <li>Get the output stream by calling {@link #getOutputStream}. The object is
 * moved to the WRITING state.
 * <li>Use the output stream. (Stays in the WRITING state.)
 * <li><em>{@link OutputStream#close() close}</em> the output stream. The object
 * moves to the WRITE_CLOSED state. The output stream becomes unusable.
 * <li>Get the input stream by calling {@link #getInputStream}. The object moves
 * to the READING state.
 * <li>Use the input stream. (The object states in the READING state.)
 * <li><em>{@link InputStream#close() close}</em> the input stream. The object
 * moves to the READ_CLOSED state. The input stream becomes unusable.
 * </ol>
 * 
 * <p>
 * Any any point the streams can be reset by calling {@link #reset}. This leaves
 * the buffer size intact, but resets the data count to zero so that the streams
 * can be reused. The object is cycled back to the READY state.
 * 
 * <p>
 * The guts of this class are based on {@link ByteArrayOutputStream} and
 * {@link ByteArrayInputStream}. The point of this class is primarily to avoid
 * the copying of the data that would occur if the standard Java classes were
 * used.
 */
@RegionLock("Lock is this protects Instance")
public final class ByteArrayStreams {
  enum State {
    READY, WRITING, WRITE_CLOSED, READING, READ_CLOSED
  }

  /** The current state of the streams. */
  State state;

  /**
   * The buffer where data is stored.
   */
  byte buf[];

  /**
   * The number of valid bytes in the buffer.
   */
  int count;

  /**
   * The current output stream if any.
   */
  @Unique
  MyOutputStream outStream;

  /**
   * The current input stream if any.
   */
  @Unique
  MyInputStream inStream;

  /**
   * Creates a new byte array output stream. The buffer capacity is initially 32
   * bytes, though its size increases if necessary.
   */
  @Unique("return")
  public ByteArrayStreams() {
    this(32);
  }

  /**
   * Creates a new byte array output stream, with a buffer capacity of the
   * specified size, in bytes.
   *
   * @param size
   *          the initial size.
   * @exception IllegalArgumentException
   *              if size is negative.
   */
  @Unique("return")
  public ByteArrayStreams(final int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Negative initial size: " + size);
    }
    buf = new byte[size];
    state = State.READY;
  }

  public synchronized SafeCloseOutputStream getOutputStream() {
    if (state != State.READY) {
      throw new IllegalStateException("Streams not in the new/reset state.");
    }
    state = State.WRITING;
    outStream = new MyOutputStream();
    return outStream;
  }

  public synchronized SafeCloseInputStream getInputStream() {
    if (state != State.WRITE_CLOSED) {
      throw new IllegalStateException("Streams not in the write closed state.");
    }
    state = State.READING;
    inStream = new MyInputStream();
    return inStream;
  }

  /**
   * Can always be called. Returns the streams to the ready state. Closes any
   * streams that may be open, and resets the byte count to zero.
   */
  public synchronized void reset() {
    count = 0;
    state = State.READY;
    if (outStream != null) {
      outStream.alive = false;
      outStream = null;
    }
    if (inStream != null) {
      inStream.alive = false;
      inStream = null;
    }
  }

  final class MyOutputStream extends SafeCloseOutputStream {
    boolean alive = true;

    /**
     * Writes the specified byte to this byte array output stream.
     * 
     * @param b
     *          the byte to be written.
     */
    @Override
    public void write(final int b) {
      synchronized (ByteArrayStreams.this) {
        checkStatus();
        int newcount = count + 1;
        if (newcount > buf.length) {
          byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
          System.arraycopy(buf, 0, newbuf, 0, count);
          buf = newbuf;
        }
        buf[count] = (byte) b;
        count = newcount;
      }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this byte array output stream.
     *
     * @param b
     *          the data.
     * @param off
     *          the start offset in the data.
     * @param len
     *          the number of bytes to write.
     */
    @Override
    public void write(final byte b[], final int off, final int len) {
      synchronized (ByteArrayStreams.this) {
        checkStatus();
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
          throw new IndexOutOfBoundsException();
        } else if (len == 0) {
          return;
        }
        int newcount = count + len;
        if (newcount > buf.length) {
          byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
          System.arraycopy(buf, 0, newbuf, 0, count);
          buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
      }
    }

    @Override
    public void close() {
      synchronized (ByteArrayStreams.this) {
        checkStatus();
        alive = false;
        ByteArrayStreams.this.outStream = null;
        state = State.WRITE_CLOSED;
      }
    }

    private void checkStatus() {
      if (!alive) {
        throw new IllegalStateException("Output stream is dead");
      }
    }
  }

  final class MyInputStream extends SafeCloseInputStream {
    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative and not larger than the value of
     * <code>count</code>. The next byte to be read from the input stream buffer
     * will be <code>buf[pos]</code>.
     */
    private int pos = 0;

    /**
     * The currently marked position in the stream. ByteArrayInputStream objects
     * are marked at position zero by default when constructed. They may be
     * marked at another position within the buffer by the <code>mark()</code>
     * method. The current buffer position is set to this point by the
     * <code>reset()</code> method.
     * <p>
     * If no mark has been set, then the value of mark is the offset passed to
     * the constructor (or 0 if the offset was not supplied).
     *
     * @since JDK1.1
     */
    int mark = 0;

    boolean alive = true;

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     * 
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream has been reached.
     */
    @Override
    public int read() {
      synchronized (ByteArrayStreams.this) {
        checkState();
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
      }
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes from
     * this input stream. If <code>pos</code> equals <code>count</code>, then
     * <code>-1</code> is returned to indicate end of file. Otherwise, the
     * number <code>k</code> of bytes read is equal to the smaller of
     * <code>len</code> and <code>count-pos</code>. If <code>k</code> is
     * positive, then bytes <code>buf[pos]</code> through
     * <code>buf[pos+k-1]</code> are copied into <code>b[off]</code> through
     * <code>b[off+k-1]</code> in the manner performed by
     * <code>System.arraycopy</code>. The value <code>k</code> is added into
     * <code>pos</code> and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     * 
     * @param b
     *          the buffer into which the data is read.
     * @param off
     *          the start offset of the data.
     * @param len
     *          the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of the
     *         stream has been reached.
     */
    @Override
    public int read(final byte b[], final int off, int len) {
      synchronized (ByteArrayStreams.this) {
        checkState();
        if (b == null) {
          throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
          throw new IndexOutOfBoundsException();
        }
        if (pos >= count) {
          return -1;
        }
        if (pos + len > count) {
          len = count - pos;
        }
        if (len <= 0) {
          return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
      }
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer bytes
     * might be skipped if the end of the input stream is reached. The actual
     * number <code>k</code> of bytes to be skipped is equal to the smaller of
     * <code>n</code> and <code>count-pos</code>. The value <code>k</code> is
     * added into <code>pos</code> and <code>k</code> is returned.
     * 
     * @param n
     *          the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    @Override
    public long skip(long n) {
      synchronized (ByteArrayStreams.this) {
        checkState();
        if (pos + n > count) {
          n = count - pos;
        }
        if (n < 0) {
          return 0;
        }
        pos += n;
        return n;
      }
    }

    /**
     * Returns the number of bytes that can be read from this input stream
     * without blocking. The value returned is <code>count&nbsp;- pos</code>,
     * which is the number of bytes remaining to be read from the input buffer.
     * 
     * @return the number of bytes that can be read from the input stream
     *         without blocking.
     */
    @Override
    public int available() {
      synchronized (ByteArrayStreams.this) {
        checkState();
        return count - pos;
      }
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The
     * <code>markSupported</code> method of <code>ByteArrayInputStream</code>
     * always returns <code>true</code>.
     * 
     * @since JDK1.1
     */
    @Override
    public boolean markSupported() {
      checkState();
      return true;
    }

    /**
     * Set the current marked position in the stream. ByteArrayInputStream
     * objects are marked at position zero by default when constructed. They may
     * be marked at another position within the buffer by this method.
     * <p>
     * If no mark has been set, then the value of the mark is the offset passed
     * to the constructor (or 0 if the offset was not supplied).
     * 
     * <p>
     * Note: The <code>readAheadLimit</code> for this class has no meaning.
     * 
     * @since JDK1.1
     */
    @Override
    public void mark(final int readAheadLimit) {
      checkState();
      mark = pos;
    }

    /**
     * Resets the buffer to the marked position. The marked position is 0 unless
     * another position was marked or an offset was specified in the
     * constructor.
     */
    @Override
    public void reset() {
      synchronized (ByteArrayStreams.this) {
        checkState();
        pos = mark;
      }
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p>
     */
    @Override
    public void close() {
      synchronized (ByteArrayStreams.this) {
        checkState();
        alive = false;
        ByteArrayStreams.this.inStream = null;
        state = State.READ_CLOSED;
      }
    }

    private void checkState() {
      if (!alive) {
        throw new IllegalStateException("Input stream is dead.");
      }
    }
  }
}
