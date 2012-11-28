package com.surelogic.common;

import java.io.PrintWriter;
import com.surelogic.Starts;

public class CharBuffer implements CharSequence {
	char[] buf = new char[64];
	int size = 0;
	
	private void ensureCapacity(int newCapacity) {
		if (buf.length < newCapacity) {
			char [] newBuf = new char[buf.length * 2];
			System.arraycopy(buf, 0, newBuf, 0, size);
			buf = newBuf;
		}
	}
	
	@Starts("nothing")
	public char charAt(int index) {
		return buf[index];
	}

	@Starts("nothing")
	public int length() {
		return size;
	}

	@Starts("nothing")
	public CharSequence subSequence(int start, int end) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		size = 0;
	}

	public CharBuffer append(char c) {
		ensureCapacity(size+1);
		buf[size] = c;
		size++;
		return this;
	}

	public CharBuffer append(String s) {
		final int len = s.length();
		ensureCapacity(size + len);
		for(int i=0; i<len; i++) {
			buf[size+i] = s.charAt(i);
		}		
		size += len;
		return this;
	}

	public void write(PrintWriter out) {
		out.write(buf, 0, size);
	}
}
