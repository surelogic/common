package com.surelogic.common.jobs.remote;

import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.Starts;
import com.surelogic.RegionEffects;
import com.surelogic.Borrowed;

public class RemoteSLJobException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final String label;
  private final int number;
  private final Object[] args;
  
  public RemoteSLJobException(String label, final int number, Object... args) {
	this.label = label;
    this.number = number;
    this.args = args;
  }
  
  public RemoteSLJobException(String label, final int number, Throwable t) {
    super(t);
	this.label = label;
    this.number = number;
    this.args = SLUtility.EMPTY_OBJECT_ARRAY;
  }
  
  public int getErrorNum() {
    return number;
  }

  @Borrowed("this")
  @RegionEffects("reads Instance, I18N:Static")
  @Starts("nothing")
  public String getToolMessage(String name) {
    if (args == null || args.length == 0) {
      return I18N.err(number, name);
    }
    Object[] args2 = new Object[args.length+1];
    args2[0] = name;
    System.arraycopy(args, 0, args2, 1, args.length);
    return I18N.err(number, args2);
  }
  
  @Borrowed("this")
  @RegionEffects("reads Instance")
  @Starts("nothing")
  @Override
  public String getMessage() {
	  return getToolMessage(label);
  }
}
