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

import com.atlassian.theplugin.commons.configuration.JiraConfigurationBean;
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
 * Date: 2008-03-10
 * Time: 12:15:37
 * To change this template use File | Settings | File Templates.
 */
public class JiraGeneralForm extends JComponent implements ContentPanel {
	private JPanel rootComponent;
	private SpinnerModel model;
	private JSpinner issuePageSize;
    private JCheckBox cbSynchroWithIntelliJTasks;
    private JCheckBox cbShowIssueTooltips;

    private transient PluginConfiguration globalPluginConfiguration;

	private transient JiraConfigurationBean jiraConfiguration;

	private transient PluginConfiguration localPluginConfigurationCopy;
	private static JiraGeneralForm instance;
	private static final int MAX_VALUE = 1000;

	private JiraGeneralForm(PluginConfiguration globalPluginConfiguration) {

		this.globalPluginConfiguration = globalPluginConfiguration;

		$$$setupUI$$$();
		model = new SpinnerNumberModel(1, 1, MAX_VALUE, 1);
		issuePageSize.setModel(model);
		this.setLayout(new BorderLayout());
		add(rootComponent, BorderLayout.CENTER);
	}


	public static JiraGeneralForm getInstance(PluginConfiguration globalPluginConfiguration) {
		if (instance == null) {
			instance = new JiraGeneralForm(globalPluginConfiguration);
		}
		return instance;
	}

	public boolean isModified() {
		return IdeaHelper.getSpinnerIntValue(issuePageSize) != jiraConfiguration.getPageSize()
               || cbSynchroWithIntelliJTasks.isSelected() != jiraConfiguration.isSynchronizeWithIntelliJTasks()
               || cbShowIssueTooltips.isSelected() != jiraConfiguration.isShowIssueTooltips();
	}

	public String getTitle() {
		return "JIRA";
	}

	public void saveData() {
		getLocalPluginConfigurationCopy().getJIRAConfigurationData().setPageSize((Integer) model.getValue());
		globalPluginConfiguration.getJIRAConfigurationData().setPageSize((Integer) model.getValue());

        getLocalPluginConfigurationCopy().getJIRAConfigurationData()
                .setSynchronizeWithIntelliJTasks(cbSynchroWithIntelliJTasks.isSelected());
        globalPluginConfiguration.getJIRAConfigurationData()
                .setSynchronizeWithIntelliJTasks(cbSynchroWithIntelliJTasks.isSelected());

        getLocalPluginConfigurationCopy().getJIRAConfigurationData().setShowIssueTooltips(cbShowIssueTooltips.isSelected());
        globalPluginConfiguration.getJIRAConfigurationData().setShowIssueTooltips(cbShowIssueTooltips.isSelected());
	}

	public void setData(PluginConfiguration config) {

		localPluginConfigurationCopy = config;

		jiraConfiguration = localPluginConfigurationCopy.getJIRAConfigurationData();

		model.setValue(jiraConfiguration.getPageSize());
        cbSynchroWithIntelliJTasks.setSelected(jiraConfiguration.isSynchronizeWithIntelliJTasks());
        cbShowIssueTooltips.setSelected(jiraConfiguration.isShowIssueTooltips());
	}

	private PluginConfiguration getLocalPluginConfigurationCopy() {
		return localPluginConfigurationCopy;
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
		rootComponent.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setOpaque(true);
		rootComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12), null));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		issuePageSize = new JSpinner();
		issuePageSize.setEnabled(true);
		issuePageSize.setToolTipText("");
		panel1.add(issuePageSize, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0,
				false));
		final JLabel label1 = new JLabel();
		label1.setText("Number of Issues to Show:");
		label1.setToolTipText("Number of issues to load from the JIRA server in one remote call");
		rootComponent.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		rootComponent.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		rootComponent.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
				1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
