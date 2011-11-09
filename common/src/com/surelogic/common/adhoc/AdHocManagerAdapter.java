package com.surelogic.common.adhoc;

/**
 * A benign adaptor for an {@link IAdHocManagerObserver}. Subclasses override
 * methods to add behavior.
 */
public abstract class AdHocManagerAdapter implements IAdHocManagerObserver {

	public void notifyQueryModelChange(AdHocManager manager) {
		// do nothing
	}

	public void notifyGlobalVariableValueChange(AdHocManager manager) {
		// do nothing
	}

	public void notifyResultModelChange(AdHocManager manager) {
		// do nothing
	}

	public void notifySelectedResultChange(AdHocQueryResult result) {
		// do nothing
	}

	public void notifyResultVariableValueChange(AdHocQueryResultSqlData result) {
		// do nothing
	}
}
