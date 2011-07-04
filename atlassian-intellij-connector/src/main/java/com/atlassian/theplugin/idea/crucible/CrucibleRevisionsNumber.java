package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CrucibleRevisionsNumber extends DialogWrapper {
	private JPanel rootComponent;
	private JSpinner numberSpinner;
	private JLabel numberLabel;

	private static final int MAX_REVISIONS = 3000;

	//CHECKSTYLE:OFF
	public CrucibleRevisionsNumber(int value) {
		super(false);
		rootComponent = new JPanel();
		rootComponent.setLayout(new FormLayout("fill:d:grow",
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		CellConstraints cc = new CellConstraints();
		rootComponent.add(panel1, cc.xy(1, 5));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel3, cc.xy(1, 1));
		numberSpinner = new JSpinner();
		panel3.add(numberSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0,
				false));
		numberLabel = new JLabel();
		numberLabel.setText("Count of requested changes:");
		panel3.add(numberLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

		init();
		numberSpinner.setModel(new SpinnerNumberModel(value, 1, MAX_REVISIONS, 1));
		pack();
	}
	//CHECKSTYLE:ON


	@Override
	public JComponent getPreferredFocusedComponent() {
		return numberSpinner;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	@Override
	protected void doOKAction() {
		super.doOKAction();
	}

	public int getValue() {
		return ((SpinnerNumberModel) numberSpinner.getModel()).getNumber().intValue();
	}
}