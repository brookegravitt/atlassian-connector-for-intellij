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

package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.idea.AboutForm;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.JiraGeneralForm;

import javax.swing.*;
import java.awt.*;

public final class ConfigPanel extends JPanel {

	private transient IdeaPluginConfigurationBean localPluginConfigurationCopy = null;

	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final BambooGeneralForm bambooConfigPanel;
	private final JiraGeneralForm jiraConfigPanel;
	private final GeneralConfigPanel generalConfigPanel;
	private final AboutForm aboutBox;


	private final transient PluginConfiguration globalConfigurationBean;


	public ConfigPanel(PluginConfiguration globalConfigurationBean, NewVersionChecker newVersionChecker) {
		this.bambooConfigPanel = BambooGeneralForm.getInstance(globalConfigurationBean);
		this.jiraConfigPanel = JiraGeneralForm.getInstance(globalConfigurationBean);
		this.generalConfigPanel = new GeneralConfigPanel(globalConfigurationBean, newVersionChecker);
		this.globalConfigurationBean = globalConfigurationBean;

		aboutBox = new AboutForm();

		initLayout();
	}


	private void initLayout() {
		setLayout(new BorderLayout());

		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));

		// add Bamboo option tab
		contentPanel.add(bambooConfigPanel.getTitle(), bambooConfigPanel);

		// add Jira option tab
		contentPanel.add(jiraConfigPanel.getTitle(), jiraConfigPanel);

		// add general tab
		contentPanel.add(generalConfigPanel.getTitle(), generalConfigPanel);

		contentPanel.add("About", aboutBox.getRootPane());

		add(contentPanel, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);
	}


	public boolean isModified() {
		return !this.localPluginConfigurationCopy.equals(globalConfigurationBean)
				|| bambooConfigPanel.isModified()
				|| jiraConfigPanel.isModified()
				|| generalConfigPanel.isModified();
	}

	public void saveData() {
		if (isModified()) {
			generalConfigPanel.saveData();
			bambooConfigPanel.saveData();
			jiraConfigPanel.saveData();
		}
	}

	public void setData() {
		this.localPluginConfigurationCopy = new IdeaPluginConfigurationBean(globalConfigurationBean);
		generalConfigPanel.setData(localPluginConfigurationCopy);
		bambooConfigPanel.setData(localPluginConfigurationCopy);
		jiraConfigPanel.setData(localPluginConfigurationCopy);
	}
}