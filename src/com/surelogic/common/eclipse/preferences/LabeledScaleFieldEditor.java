package com.surelogic.common.eclipse.preferences;

import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class LabeledScaleFieldEditor extends ScaleFieldEditor {
	Label scaleLabel;
	
	public LabeledScaleFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	public int getNumberOfControls() {
		return super.getNumberOfControls()+1;
	}
	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns-1);
		if (scaleLabel == null) {
			scaleLabel = new Label(parent, SWT.NONE);
			scaleLabel.setText("?");
		}
	}
	
	@Override
	protected void doLoad() {
		super.doLoad();
		scaleLabel.setText(Integer.toString(scale.getSelection()));
	}
	
	@Override
	protected void valueChanged() {
		scaleLabel.setText(Integer.toString(scale.getSelection()));
		super.valueChanged();
	}
}
