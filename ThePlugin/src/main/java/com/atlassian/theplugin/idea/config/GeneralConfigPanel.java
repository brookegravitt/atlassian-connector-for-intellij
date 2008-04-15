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

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.GeneralConfigForm;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;

import javax.swing.*;
import java.awt.*;

/**
 * General plugin config form.
 */
public class GeneralConfigPanel extends JPanel implements ContentPanel {
	private GeneralConfigForm dialog;
	private PluginConfiguration localPluginConfigurationCopy;
	private final PluginConfiguration globalPluginConfiguration;

	public GeneralConfigPanel(PluginConfiguration globalPluginConfiguration, NewVersionChecker checker) {
		super();
		this.globalPluginConfiguration = globalPluginConfiguration;
		localPluginConfigurationCopy = this.globalPluginConfiguration;
		setLayout(new CardLayout());
		dialog = new GeneralConfigForm(checker, localPluginConfigurationCopy);
		add(dialog.getRootPane(), "GeneralConfig");
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		return !localPluginConfigurationCopy.equals(globalPluginConfiguration)
				|| dialog.getIsAutoUpdateEnabled()
					!= globalPluginConfiguration.getGeneralConfigurationData().isAutoUpdateEnabled()
				|| dialog.getIsCheckUnstableVersionsEnabled()
					!= globalPluginConfiguration.getGeneralConfigurationData().isCheckUnstableVersionsEnabled()
				|| dialog.getIsAnonymousFeedbackEnabled()
					!= globalPluginConfiguration.getGeneralConfigurationData().getAnonymousFeedbackEnabled();

	}
	
	public void setIsAnonymousFeedbackEnabled(Boolean isAnonymousFeedbackEnabled) {
		dialog.setIsAnonymousFeedbackEnabled(isAnonymousFeedbackEnabled);
	}

	public String getTitle() {
		return "General";
	}

	public void getData() {
		localPluginConfigurationCopy.getGeneralConfigurationData().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData().setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		localPluginConfigurationCopy.getGeneralConfigurationData().setAnonymousFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());

		globalPluginConfiguration.getGeneralConfigurationData().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		globalPluginConfiguration.getGeneralConfigurationData().setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		globalPluginConfiguration.getGeneralConfigurationData().setAnonymousFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());
	}

	public void setData(PluginConfiguration config) {
		localPluginConfigurationCopy = config;
		dialog.setAutoUpdateEnabled(localPluginConfigurationCopy.getGeneralConfigurationData().isAutoUpdateEnabled());
		dialog.setIsCheckUnstableVersionsEnabled(localPluginConfigurationCopy.getGeneralConfigurationData().isCheckUnstableVersionsEnabled());
		dialog.setIsAnonymousFeedbackEnabled(localPluginConfigurationCopy.getGeneralConfigurationData().getAnonymousFeedbackEnabled());
	}

}
