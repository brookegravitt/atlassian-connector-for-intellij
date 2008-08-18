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

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.CrucibleGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.JiraGeneralForm;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;

public final class ConfigPanel extends JPanel {

	private transient PluginConfigurationBean localPluginConfigurationCopy = null;

	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	//private final ServerConfigPanel serverConfigPanel;
	private final BambooGeneralForm bambooConfigPanel;
	private final CrucibleGeneralForm crucibleConfigPanel;
	private final JiraGeneralForm jiraConfigPanel;
	private final GeneralConfigPanel generalConfigPanel;

	private final transient PluginConfiguration globalConfigurationBean;
//    private final ProjectId projectId;
    private final CfgManager cfgManager;

//	private ProjectConfiguration projectConfiguration;

//	public ProjectConfiguration getProjectConfiguration() {
//		return projectConfiguration;
//	}

	public ConfigPanel(PluginConfiguration globalConfigurationBean, /*ProjectId projectId, */CfgManager cfgManager) {
//        this.projectId = projectId;
        this.cfgManager = cfgManager;
//		projectConfiguration = cfgManager.getProjectConfiguration(projectId).getClone();
//		final Collection<ServerCfg> allServers = projectConfiguration.getServers();
		//this.serverConfigPanel = new ServerConfigPanel(null, allServers);
		this.bambooConfigPanel = new BambooGeneralForm(cfgManager.getGlobalBambooCfg());
		this.crucibleConfigPanel = CrucibleGeneralForm.getInstance(globalConfigurationBean);
		this.jiraConfigPanel = JiraGeneralForm.getInstance(globalConfigurationBean);
		this.generalConfigPanel = GeneralConfigPanel.getInstance(globalConfigurationBean);
		this.globalConfigurationBean = globalConfigurationBean;
		initLayout();
	}


	private void initLayout() {
		setLayout(new BorderLayout());

		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));

		// add servers tab
		//contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

		// add Bamboo option tab
		contentPanel.add(bambooConfigPanel.getTitle(), bambooConfigPanel);

		// add Crucible option tab
		contentPanel.add(crucibleConfigPanel.getTitle(), crucibleConfigPanel);

		// add Jira option tab
		contentPanel.add(jiraConfigPanel.getTitle(), jiraConfigPanel);

		// add general tab
		contentPanel.add(generalConfigPanel.getTitle(), generalConfigPanel);

		add(contentPanel, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);
	}



	public boolean isModified() {
		if (globalConfigurationBean.getGeneralConfigurationData().getAnonymousFeedbackEnabled() == null) {
			int answer = Messages.showYesNoDialog("We would greatly appreciate if you allow us to collect anonymous "
					+ "usage statistics to help us provide a better quality product. Is this ok?",
					PluginUtil.getInstance().getName() + " request", Messages.getQuestionIcon());
			localPluginConfigurationCopy.getGeneralConfigurationData().
					setAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
			globalConfigurationBean.getGeneralConfigurationData().
					setAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
			generalConfigPanel.setIsAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
		}
		return !this.localPluginConfigurationCopy.equals(globalConfigurationBean)
				/*|| projectConfiguration.equals(cfgManager.getProjectConfiguration(projectId)) == false*/
				/*|| serverConfigPanel.isModified()*/
				|| bambooConfigPanel.isModified()
				|| crucibleConfigPanel.isModified()
				|| jiraConfigPanel.isModified()
				|| generalConfigPanel.isModified();
	}

	public void saveData() {
		/*serverConfigPanel.saveData();*/
		if (isModified()) {
			/*cfgManager.updateProjectConfiguration(projectId, projectConfiguration);*/
			generalConfigPanel.saveData();
			bambooConfigPanel.saveData();
			jiraConfigPanel.saveData();
			crucibleConfigPanel.saveData();
		}
	}

	public void setData() {
		this.localPluginConfigurationCopy = new PluginConfigurationBean(globalConfigurationBean);
		/*projectConfiguration = cfgManager.getProjectConfiguration(projectId).getClone();*/
		/*serverConfigPanel.setData(projectConfiguration.getServers());*/
		generalConfigPanel.setData(localPluginConfigurationCopy);
		bambooConfigPanel.setData(cfgManager.getGlobalBambooCfg());
		jiraConfigPanel.setData(localPluginConfigurationCopy);
		crucibleConfigPanel.setData(localPluginConfigurationCopy);
	}
}