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

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.CrucibleGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.JiraGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;

public final class ConfigPanel extends JPanel {
	private static ConfigPanel instance;

	private transient PluginConfigurationBean localPluginConfigurationCopy = null;

	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final ServerConfigPanel serverConfigPanel;
	private final BambooGeneralForm bambooConfigPanel;
	private final CrucibleGeneralForm crucibleConfigPanel;
	private final JiraGeneralForm jiraConfigPanel;
	private final GeneralConfigPanel generalConfigPanel;

	private final transient PluginConfigurationBean globalConfigurationBean;

	public ConfigPanel(ServerConfigPanel serverConfigPanel,
					   BambooGeneralForm bambooConfigPanel,
					   CrucibleGeneralForm crucibleConfigPanel,
					   JiraGeneralForm jiraConfigPanel,
					   GeneralConfigPanel generalConfigPanel,
					   PluginConfigurationBean globalConfigurationBean) {
		
		/* Yes, I mean this. Assigning to a static field from within a constructor. Blame *Action. */
		instance = this;
		this.serverConfigPanel = serverConfigPanel;
		this.bambooConfigPanel = bambooConfigPanel;
		this.crucibleConfigPanel = crucibleConfigPanel;
		this.jiraConfigPanel = jiraConfigPanel;
		this.generalConfigPanel = generalConfigPanel;
		this.globalConfigurationBean = globalConfigurationBean;

		initLayout();
	}

	/**
	 * This one is still here because IDEA complains about AnAction objects having non-parameterless constructor.
	 * @return single instance of ConfigPanel.
	 */
	public static ConfigPanel getInstance() {
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());

		contentPanel.setOpaque(true);
		contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));

		// add servers tab
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

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
					PluginUtil.getName() + " request", Messages.getQuestionIcon());
			localPluginConfigurationCopy.getGeneralConfigurationData().
					setAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
			globalConfigurationBean.getGeneralConfigurationData().
					setAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
			generalConfigPanel.setIsAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
		}
		return !this.localPluginConfigurationCopy.equals(globalConfigurationBean)
				|| serverConfigPanel.isModified()
				|| bambooConfigPanel.isModified()
				|| crucibleConfigPanel.isModified()
				|| jiraConfigPanel.isModified()
				|| generalConfigPanel.isModified();
	}

	public void getData() {
		if (isModified()) {
			serverConfigPanel.getData();
			generalConfigPanel.getData();
			bambooConfigPanel.getData();
			jiraConfigPanel.getData();
			crucibleConfigPanel.getData();
		}
	}

	public void setData() {
		this.localPluginConfigurationCopy = new PluginConfigurationBean(globalConfigurationBean);
		serverConfigPanel.setData(localPluginConfigurationCopy);
		generalConfigPanel.setData(localPluginConfigurationCopy);
		bambooConfigPanel.setData(localPluginConfigurationCopy);
		jiraConfigPanel.setData(localPluginConfigurationCopy);
		crucibleConfigPanel.setData(localPluginConfigurationCopy);
	}

	public void addServer(ServerType serverType) {
		serverConfigPanel.addServer(serverType);
	}

	public void removeServer() {
		serverConfigPanel.removeServer();
	}


	public void copyServer() {
		serverConfigPanel.copyServer();
	}
}