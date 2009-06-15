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
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.GeneralConfigForm;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;

import javax.swing.*;
import java.awt.*;

/**
 * General plugin config form.
 */
public final class GeneralConfigPanel extends JPanel implements ContentPanel {
	private GeneralConfigForm dialog;

	private PluginConfiguration localPluginConfigurationCopy;

	private final PluginConfiguration globalPluginConfiguration;

	public GeneralConfigPanel(PluginConfiguration globalPluginConfiguration, final NewVersionChecker newVersionChecker) {
		this.globalPluginConfiguration = globalPluginConfiguration;
		localPluginConfigurationCopy = this.globalPluginConfiguration;
		setLayout(new CardLayout());
		dialog = new GeneralConfigForm(newVersionChecker);
		add(dialog.getRootPane(), "GeneralConfig");
	}

	public boolean isModified() {
		return !localPluginConfigurationCopy.equals(globalPluginConfiguration)
				|| dialog.getIsAutoUpdateEnabled()
				!= globalPluginConfiguration.getGeneralConfigurationData().isAutoUpdateEnabled()
				|| dialog.isHttpServerEnabled()
				!= globalPluginConfiguration.getGeneralConfigurationData().isHttpServerEnabled()
				|| dialog.getHttpServerPort()
				!= globalPluginConfiguration.getGeneralConfigurationData().getHttpServerPort()
				|| dialog.getIsCheckUnstableVersionsEnabled()
				!= globalPluginConfiguration.getGeneralConfigurationData().isCheckUnstableVersionsEnabled()
				|| MiscUtil.isModified(dialog.getIsAnonymousFeedbackEnabled(),
				globalPluginConfiguration.getGeneralConfigurationData().getAnonymousEnhancedFeedbackEnabled())
				|| dialog.getUseIdeaProxySettings()
				!= globalPluginConfiguration.getGeneralConfigurationData().getUseIdeaProxySettings()
				|| MiscUtil.isModified(dialog.getCheckNotButtonOption(),
				globalPluginConfiguration.getGeneralConfigurationData().getCheckNowButtonOption());

	}

	public void setIsAnonymousFeedbackEnabled(Boolean isAnonymousFeedbackEnabled) {
		dialog.setIsAnonymousFeedbackEnabled(isAnonymousFeedbackEnabled);
	}

	public String getTitle() {
		return "General";
	}

	public void saveData() {
		localPluginConfigurationCopy.getGeneralConfigurationData().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData().setHttpServerEnabled(dialog.isHttpServerEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData().setHttpServerPort(dialog.getHttpServerPort());
		localPluginConfigurationCopy.getGeneralConfigurationData()
				.setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData()
				.setAnonymousEnhancedFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData().setUseIdeaProxySettings(dialog.getUseIdeaProxySettings());
		localPluginConfigurationCopy.getGeneralConfigurationData().setCheckNowButtonOption(dialog.getCheckNotButtonOption());

		globalPluginConfiguration.getGeneralConfigurationData().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		globalPluginConfiguration.getGeneralConfigurationData().setHttpServerEnabled(dialog.isHttpServerEnabled());
		globalPluginConfiguration.getGeneralConfigurationData().setHttpServerPort(dialog.getHttpServerPort());
		globalPluginConfiguration.getGeneralConfigurationData()
				.setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		globalPluginConfiguration.getGeneralConfigurationData()
				.setAnonymousEnhancedFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());
		globalPluginConfiguration.getGeneralConfigurationData().setUseIdeaProxySettings(dialog.getUseIdeaProxySettings());
		globalPluginConfiguration.getGeneralConfigurationData().setCheckNowButtonOption(dialog.getCheckNotButtonOption());
	}

	public void setData(PluginConfiguration config) {
		localPluginConfigurationCopy = config;
		dialog.setAutoUpdateEnabled(localPluginConfigurationCopy.getGeneralConfigurationData()
				.isAutoUpdateEnabled());
		dialog.setHttpServerEnabled(localPluginConfigurationCopy.getGeneralConfigurationData()
				.isHttpServerEnabled());
		dialog.setHttpServerPort(localPluginConfigurationCopy.getGeneralConfigurationData()
				.getHttpServerPort());
		dialog.setIsCheckUnstableVersionsEnabled(localPluginConfigurationCopy.getGeneralConfigurationData()
				.isCheckUnstableVersionsEnabled());
		dialog.setIsAnonymousFeedbackEnabled(localPluginConfigurationCopy.getGeneralConfigurationData()
				.getAnonymousEnhancedFeedbackEnabled());
		dialog.setUseIdeaProxySettings(localPluginConfigurationCopy.getGeneralConfigurationData()
				.getUseIdeaProxySettings());
		dialog.setCheckNowButtonOption(localPluginConfigurationCopy.getGeneralConfigurationData()
				.getCheckNowButtonOption());
	}
}
