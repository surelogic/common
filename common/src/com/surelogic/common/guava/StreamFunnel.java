package com.surelogic.common.guava;

import java.io.*;
import java.util.logging.Level;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.surelogic.common.logging.SLLogger;

public class StreamFunnel implements Funnel<InputStream> {
	private final byte[] buf = new byte[8192];
	
	@Override
	public void funnel(InputStream from, PrimitiveSink into) {
        final BufferedInputStream bis = new BufferedInputStream(from, 8192);
        int num;
        try {
        	while ((num = bis.read(buf)) >= 0) {
        		into.putBytes(buf, 0, num);
        	}
        } catch(IOException e) {
        	SLLogger.getLogger().log(Level.WARNING, "Got exception while reading stream", e);
        }
	}
}
