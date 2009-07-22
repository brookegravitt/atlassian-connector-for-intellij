/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BambooGeneralForm extends JComponent {
	private JRadioButton allFailuresFirstSuccess;
	private JRadioButton firstFailureFirstSuccess;
	private JRadioButton never;
	private JPanel rootComponent;
	private JSpinner pollTimeSpinner;
	private JCheckBox onlyMyBuilds;
	private SpinnerModel model;

	private transient BambooConfigurationBean localBambooConfigurationCopy;

	private final transient PluginConfiguration globalPluginConfiguration;

	private static BambooGeneralForm instance;
	private static final int MAX_VALUE = 998;

	private BambooGeneralForm(PluginConfiguration globalConfigurationBean /*BambooCfg bambooCfg*/) {
		this.globalPluginConfiguration = globalConfigurationBean;
		$$$setupUI$$$();
		setLayout(new CardLayout());
		model = new SpinnerNumberModel(1, 1, MAX_VALUE, 1);
		pollTimeSpinner.setModel(model);
		add(rootComponent, "BambooGeneralForm");

		ActionListener popupSettingsListener = new PopupSettingsListener();
		never.addActionListener(popupSettingsListener);
		firstFailureFirstSuccess.addActionListener(popupSettingsListener);
		allFailuresFirstSuccess.addActionListener(popupSettingsListener);
	}


	public static BambooGeneralForm getInstance(PluginConfiguration globalPluginConfiguration) {
		if (instance == null) {
			instance = new BambooGeneralForm(globalPluginConfiguration);
		}
		return instance;
	}


	public void setData(PluginConfiguration config /*@NotNull BambooCfg newBambooCfg*/) {
		//bambooConfiguration = newBambooCfg;

		localBambooConfigurationCopy = config.getBambooConfigurationData();

		onlyMyBuilds.setSelected(localBambooConfigurationCopy.isOnlyMyBuilds());
		onlyMyBuilds.setEnabled(true);

		BambooTooltipOption configOption = localBambooConfigurationCopy.getBambooTooltipOption();

		if (configOption != null) {
			switch (configOption) {
				case ALL_FAULIRES_AND_FIRST_SUCCESS:
					allFailuresFirstSuccess.setSelected(true);
					break;
				case FIRST_FAILURE_AND_FIRST_SUCCESS:
					firstFailureFirstSuccess.setSelected(true);
					break;
				case NEVER:
				default:
					never.setSelected(true);
					onlyMyBuilds.setEnabled(false);
					break;
			}
		} else {
			setDefaultTooltipOption();
		}
		model.setValue(localBambooConfigurationCopy.getPollTime());
	}

	public void saveData() {
		localBambooConfigurationCopy.setBambooTooltipOption(getBambooTooltipOption());
		localBambooConfigurationCopy.setPollTime((Integer) model.getValue());
		localBambooConfigurationCopy.setOnlyMyBuilds(onlyMyBuilds.isSelected());
		globalPluginConfiguration.getBambooConfigurationData().setBambooTooltipOption(getBambooTooltipOption());
		globalPluginConfiguration.getBambooConfigurationData().setPollTime((Integer) model.getValue());
		globalPluginConfiguration.getBambooConfigurationData().setOnlyMyBuilds(onlyMyBuilds.isSelected());
	}

	private BambooTooltipOption getBambooTooltipOption() {
		if (allFailuresFirstSuccess.isSelected()) {
			return BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS;
		} else if (firstFailureFirstSuccess.isSelected()) {
			return BambooTooltipOption.FIRST_FAILURE_AND_FIRST_SUCCESS;
		} else if (never.isSelected()) {
			return BambooTooltipOption.NEVER;
		} else {
			return getDefaultTooltipOption();
		}
	}

	public boolean isModified() {
		if (localBambooConfigurationCopy.getBambooTooltipOption() != null) {
			if (localBambooConfigurationCopy.getBambooTooltipOption() != getBambooTooltipOption()) {
				return true;
			}
		} else if (getBambooTooltipOption() != BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS) {
			return true;
		}

		if (localBambooConfigurationCopy.isOnlyMyBuilds() != onlyMyBuilds.isSelected()) {
			return true;
		}


		return IdeaHelper.getSpinnerIntValue(pollTimeSpinner) != localBambooConfigurationCopy.getPollTime();

	}

	public String getTitle() {
		return "Bamboo";
	}

	private void setDefaultTooltipOption() {
		allFailuresFirstSuccess.setSelected(true);
	}

	private BambooTooltipOption getDefaultTooltipOption() {
		return BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS;
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootComponent = new JPanel();
		rootComponent.setLayout(new FormLayout(
				"fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:d:grow",
				"center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
		rootComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12), null));
		allFailuresFirstSuccess = new JRadioButton();
		allFailuresFirstSuccess.setSelected(true);
		allFailuresFirstSuccess.setText("All Build Failures and First Build Success");
		CellConstraints cc = new CellConstraints();
		rootComponent.add(allFailuresFirstSuccess, cc.xy(3, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
		firstFailureFirstSuccess = new JRadioButton();
		firstFailureFirstSuccess.setText("First Build Failure and First Build Success");
		rootComponent.add(firstFailureFirstSuccess, cc.xy(3, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
		never = new JRadioButton();
		never.setText("Never");
		rootComponent.add(never, cc.xy(3, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
		final Spacer spacer1 = new Spacer();
		rootComponent.add(spacer1, cc.xy(3, 11, CellConstraints.DEFAULT, CellConstraints.FILL));
		final Spacer spacer2 = new Spacer();
		rootComponent.add(spacer2, cc.xy(5, 11, CellConstraints.FILL, CellConstraints.DEFAULT));
		final JLabel label1 = new JLabel();
		label1.setText("Popups:");
		rootComponent.add(label1, cc.xy(1, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		final JLabel label2 = new JLabel();
		label2.setText("Background refresh  every:");
		rootComponent.add(label2, cc.xy(1, 9));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel1, cc.xy(3, 9));
		pollTimeSpinner = new JSpinner();
		panel1.add(pollTimeSpinner, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0,
				false));
		final JLabel label3 = new JLabel();
		label3.setText("minute(s)");
		panel1.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		panel1.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		onlyMyBuilds = new JCheckBox();
		onlyMyBuilds.setText("Only for My Builds");
		onlyMyBuilds.setToolTipText("Shows notification popup only for builds triggered by commit of the logged in user");
		rootComponent.add(onlyMyBuilds,
				new CellConstraints(3, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 20, 0, 0)));
		label3.setLabelFor(pollTimeSpinner);
		ButtonGroup buttonGroup;
		buttonGroup = new ButtonGroup();
		buttonGroup.add(allFailuresFirstSuccess);
		buttonGroup.add(firstFailureFirstSuccess);
		buttonGroup.add(never);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}

	//	public PluginConfiguration getPluginConfiguration() {
//		return localPluginConfigurationCopy;
//	}

	private class PopupSettingsListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			switch (getBambooTooltipOption()) {
				case ALL_FAULIRES_AND_FIRST_SUCCESS:
				case FIRST_FAILURE_AND_FIRST_SUCCESS:
					onlyMyBuilds.setEnabled(true);
					break;
				case NEVER:
				default:
					onlyMyBuilds.setEnabled(false);
					break;
			}
		}
	}
}
