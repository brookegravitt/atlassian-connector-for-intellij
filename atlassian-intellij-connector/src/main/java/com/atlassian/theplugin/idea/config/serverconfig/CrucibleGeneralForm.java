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

import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.configuration.CrucibleTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ContentPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-07
 * Time: 11:30:05
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleGeneralForm extends JComponent implements ContentPanel {
	private JPanel rootComponent;
	private JSpinner pollTimeSpinner;
	private JRadioButton unreadCrucibleReviews;
	private JRadioButton never;
	private JSpinner reviewCreationTimeoutSpinner;
	private SpinnerModel pollTimeModel;
	private SpinnerModel reviewCreationTimeoutModel;

	private transient PluginConfiguration globalPluginConfiguration;

	private transient CrucibleConfigurationBean crucibleConfiguration;

	private transient PluginConfiguration localPluginConfigurationCopy;
	private static CrucibleGeneralForm instance;
	private static final int MAX_VALUE = 1000;

	private CrucibleGeneralForm(PluginConfiguration globalPluginConfiguration) {

		this.globalPluginConfiguration = globalPluginConfiguration;

		$$$setupUI$$$();

		pollTimeModel = new SpinnerNumberModel(1, 1, MAX_VALUE, 1);
		pollTimeSpinner.setModel(pollTimeModel);

		reviewCreationTimeoutModel = new SpinnerNumberModel(1, 1, MAX_VALUE, 1);
		reviewCreationTimeoutSpinner.setModel(reviewCreationTimeoutModel);

		this.setLayout(new BorderLayout());
		add(rootComponent, BorderLayout.WEST);
	}

	public static CrucibleGeneralForm getInstance(PluginConfiguration globalPluginConfiguration) {
		if (instance == null) {
			instance = new CrucibleGeneralForm(globalPluginConfiguration);
		}
		return instance;
	}


	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		if (crucibleConfiguration.getCrucibleTooltipOption() != null) {
			if (crucibleConfiguration.getCrucibleTooltipOption() != getCrucibleTooltipOption()) {
				return true;
			}
		} else if (getCrucibleTooltipOption() != CrucibleTooltipOption.UNREAD_REVIEWS) {
			return true;
		}
		if (crucibleConfiguration.getReviewCreationTimeout()
				!= IdeaHelper.getSpinnerIntValue(reviewCreationTimeoutSpinner)) {
			return true;
		}
		return IdeaHelper.getSpinnerIntValue(pollTimeSpinner) != crucibleConfiguration.getPollTime();
	}

	public String getTitle() {
		return "Crucible";
	}

	private CrucibleTooltipOption getCrucibleTooltipOption() {
		if (unreadCrucibleReviews.isSelected()) {
			return CrucibleTooltipOption.UNREAD_REVIEWS;
		} else if (never.isSelected()) {
			return CrucibleTooltipOption.NEVER;
		} else {
			return getDefaultTooltipOption();
		}
	}

	public void saveData() {
		getLocalPluginConfigurationCopy().getCrucibleConfigurationData().setCrucibleTooltipOption(getCrucibleTooltipOption());
		globalPluginConfiguration.getCrucibleConfigurationData().setCrucibleTooltipOption(getCrucibleTooltipOption());
		getLocalPluginConfigurationCopy().getCrucibleConfigurationData().setPollTime((Integer) pollTimeModel.getValue());
		globalPluginConfiguration.getCrucibleConfigurationData().setPollTime((Integer) pollTimeModel.getValue());
		getLocalPluginConfigurationCopy().getCrucibleConfigurationData()
				.setReviewCreationTimeout((Integer) reviewCreationTimeoutModel.getValue());
		globalPluginConfiguration.getCrucibleConfigurationData()
				.setReviewCreationTimeout((Integer) reviewCreationTimeoutModel.getValue());
	}

	public void setData(PluginConfiguration config) {

		localPluginConfigurationCopy = config;

		crucibleConfiguration = localPluginConfigurationCopy.getCrucibleConfigurationData();
		CrucibleTooltipOption configOption = this.crucibleConfiguration.getCrucibleTooltipOption();

		if (configOption != null) {
			switch (configOption) {
				case UNREAD_REVIEWS:
					unreadCrucibleReviews.setSelected(true);
					break;
				case NEVER:
					never.setSelected(true);
					break;
				default:
					never.setSelected(true);
					break;
			}
		} else {
			setDefaultTooltipOption();
		}

		pollTimeModel.setValue(crucibleConfiguration.getPollTime());
		reviewCreationTimeoutModel.setValue(crucibleConfiguration.getReviewCreationTimeout());
	}

	private void setDefaultTooltipOption() {
		unreadCrucibleReviews.setSelected(true);
	}

	private CrucibleTooltipOption getDefaultTooltipOption() {
		return CrucibleTooltipOption.UNREAD_REVIEWS;
	}

	public PluginConfiguration getLocalPluginConfigurationCopy() {
		return localPluginConfigurationCopy;
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
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
		rootComponent.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12), null));
		final JLabel label1 = new JLabel();
		label1.setText("Background refresh every:");
		rootComponent.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		rootComponent.add(spacer1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		rootComponent.add(spacer2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
				1, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(119, 14), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Popups:");
		rootComponent.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		unreadCrucibleReviews = new JRadioButton();
		unreadCrucibleReviews.setText("When there are new Crucible reviews");
		rootComponent.add(unreadCrucibleReviews, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		never = new JRadioButton();
		never.setText("Never");
		rootComponent.add(never, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Timeout review creation after:");
		rootComponent.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		pollTimeSpinner = new JSpinner();
		panel1.add(pollTimeSpinner, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, 24), null, 0,
				false));
		final JLabel label4 = new JLabel();
		label4.setText("minute(s)");
		panel1.add(label4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		reviewCreationTimeoutSpinner = new JSpinner();
		panel2.add(reviewCreationTimeoutSpinner, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
				new Dimension(50, 24), null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("minute(s)");
		panel2.add(label5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label4.setLabelFor(pollTimeSpinner);
		ButtonGroup buttonGroup;
		buttonGroup = new ButtonGroup();
		buttonGroup.add(unreadCrucibleReviews);
		buttonGroup.add(never);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
