package com.surelogic.common.eclipse.adhoc.dialogs;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Dialog to allow the user to view and optionally edit variable values.
 */
public final class VariableValueDialog extends Dialog {

	/**
	 * Displays a dialog of read only variable values to the user. If the set of
	 * variable values passed is {@code null} or empty an appropriate message is
	 * displayed to the user.
	 * 
	 * @param variableValues
	 *            the set of variable values to display in the dialog, or
	 *            {@code null} if none.
	 */
	public static void openReadOnly(Map<String, String> variableValues) {
		if (variableValues == null || variableValues.isEmpty()) {
			MessageDialog
					.openInformation(
							SWTUtility.getShell(),
							I18N
									.msg("adhoc.query.dialog.variableValues.noVariables.title"),
							I18N
									.msg("adhoc.query.dialog.variableValues.noVariables.msg"));
		} else {
			VariableValueDialog dialog = new VariableValueDialog(SWTUtility
					.getShell(), variableValues.keySet(), variableValues, true);
			dialog.open();
		}
	}

	final Map<String, Text> f_variableToText = new HashMap<String, Text>();

	final Map<String, String> f_workValues = new HashMap<String, String>();

	final Map<String, String> f_enteredValues = new HashMap<String, String>();

	final boolean f_readOnly;

	/**
	 * Creates a variable value dialog.
	 * 
	 * @param parentShell
	 *            the shell to use.
	 * @param variables
	 *            the set of variables to allow the user to edit, or {@code
	 *            null} if none.
	 * @param variableValues
	 *            the set of variable values to display in the dialog, or
	 *            {@code null} if none.
	 * @param readOnly
	 *            {@code true} if the user is only allowed to view the values,
	 *            {@code false} if changes are allowed.
	 */
	public VariableValueDialog(Shell parentShell, Set<String> variables,
			Map<String, String> variableValues, boolean readOnly) {
		super(parentShell);
		f_readOnly = readOnly;
		if (variableValues != null)
			f_workValues.putAll(variableValues);
		if (variables != null)
			for (String variable : variables) {
				if (!f_workValues.containsKey(variable)) {
					f_workValues.put(variable, "");
				}
			}
	}

	/**
	 * Gets the set of entered variable values when this dialog was dismissed.
	 * 
	 * @return the set of entered variable values when this dialog was
	 *         dismissed.
	 */
	public Map<String, String> getEnteredValues() {
		return f_enteredValues;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (f_readOnly) {
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
		} else {
			super.createButtonsForButtonBar(parent);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.NONE);
		directions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		directions.setText(I18N.msg("adhoc.query.dialog.variableValues.msg"
				+ (f_readOnly ? ".readOnly" : "")));

		/*
		 * Sort the key set of the properties so we get a dialog in alphabetical
		 * order. This is annoying, but the keys to Properties are of type
		 * Object rather than type String so it is necessary.
		 */
		List<String> variables = new LinkedList<String>();
		for (Object o : f_workValues.keySet()) {
			String key = o.toString();
			variables.add(key);
		}
		Collections.sort(variables);

		for (String variable : variables) {
			final Label variableLabel = new Label(panel, SWT.NONE);
			variableLabel.setText(variable);
			variableLabel.setForeground(getShell().getDisplay().getSystemColor(
					SWT.COLOR_BLUE));
			variableLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false));
			final Text variableValue = new Text(panel, f_readOnly ? SWT.SINGLE
					| SWT.READ_ONLY : SWT.SINGLE);
			variableValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					false, true));
			variableValue.setText(f_workValues.get(variable));
			f_variableToText.put(variable, variableValue);
		}
		return panel;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(I18N.msg("adhoc.query.dialog.variableValues.title"
				+ (f_readOnly ? ".readOnly" : "")));
	}

	@Override
	protected void okPressed() {
		f_enteredValues.clear();
		for (String key : f_variableToText.keySet()) {
			String value = f_variableToText.get(key).getText();
			if (value != null)
				f_enteredValues.put(key, value);
		}
		super.okPressed();
	}
}
