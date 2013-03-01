package com.surelogic.common.adhoc;

import com.surelogic.*;

/**
 * A benign adaptor for an {@link IAdHocManagerObserver}. Subclasses override
 * methods to add behavior.
 */
@ReferenceObject
public abstract class AdHocManagerAdapter implements IAdHocManagerObserver {

  @Override
  public void notifyQueryModelChange(AdHocManager manager) {
    // do nothing
  }

  @Override
  public void notifyGlobalVariableValueChange(AdHocManager manager) {
    // do nothing
  }

  @Override
  public void notifyResultModelChange(AdHocManager manager) {
    // do nothing
  }

  @Override
  public void notifySelectedResultChange(AdHocQueryResult result) {
    // do nothing
  }

  @Override
  public void notifyResultVariableValueChange(AdHocQueryResultSqlData result) {
    // do nothing
  }

  @Override
  public void notifyQuerydocValueChange(AdHocQuery query) {
    // do nothing
  }
}
