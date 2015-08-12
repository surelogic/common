import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;

public class UnicodeChecker {
  public static void main(String[] args) {
    go(new File("../.."));
  }

  public static void go(File path) {
    if (path.isDirectory()) {
      for (File e : path.listFiles()) {
        go(e);
      }
    } else
      check(path);
  }

  public static void check(File javaFile) {
    final String fn = javaFile.getAbsolutePath();
    if (javaFile.exists() && javaFile.isFile() && fn.endsWith(".java")) {
      // System.out.println("found Java file : " + fn);
      read(javaFile, fn);
    }
  }

  public static void read(File javaFile, String name) {
    FileInputStream fIn;
    FileChannel fChan;
    long fSize;
    ByteBuffer mBuf;

    try {
      fIn = new FileInputStream(javaFile);
      fChan = fIn.getChannel();
      fSize = fChan.size();
      mBuf = ByteBuffer.allocate((int) fSize);
      fChan.read(mBuf);
      mBuf.rewind();

      CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPORT);
      decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
      try {
        CharBuffer parsed = decoder.decode(mBuf);
      } catch (MalformedInputException e) {
    	System.err.println(name + " MIE: " + e.getMessage() + " [bytepostion=" + mBuf.position() + "]");
    	/*
    	CharBuffer parsed = CharBuffer.allocate(mBuf.position());
    	decoder.decode(mBuf, parsed, true);
    	final int len = parsed.length();
    	System.out.println("\t"+parsed.subSequence(len-30, len));
    	*/
      }

      // for (int i = 0; i < fSize; i++)
      // System.out.print((char) mBuf.get());
      fChan.close();
      fIn.close();
    } catch (IOException exc) {
      System.out.println(exc);
    }
  }
}
