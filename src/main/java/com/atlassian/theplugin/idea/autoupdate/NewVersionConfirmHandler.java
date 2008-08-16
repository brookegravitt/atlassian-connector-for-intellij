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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

public class NewVersionConfirmHandler implements UpdateActionHandler {
	private static final String DOWNLOAD_TITLE = "Downloading new " + PluginUtil.getInstance().getName() + " plugin version ";

	@Nullable
	private final Project project;
	private GeneralConfigurationBean updateConfiguration;

	public NewVersionConfirmHandler(@Nullable Project project, GeneralConfigurationBean updateConfiguration) {
		this.project = project;
		this.updateConfiguration = updateConfiguration;
	}

	public void doAction(final InfoServer.VersionInfo versionInfo, boolean showConfigPath) throws ThePluginException {
		Version aVersion = versionInfo.getVersion();
		String message = "New plugin version " + aVersion + " is available. "
				+ "Your version is " + PluginUtil.getInstance().getVersion()
				+ ". Do you want to download and install?";
		String title = "New plugin version download";

		int answer = Messages.showYesNoDialog(message, title, Messages.getQuestionIcon());

		//int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
		//		message, title, JOptionPane.YES_NO_OPTION);
		//if (answer == JOptionPane.OK_OPTION) {
		if (answer == DialogWrapper.OK_EXIT_CODE) {
			Task.Backgroundable downloader = new Task.Backgroundable(project, DOWNLOAD_TITLE, false) {
				@Override
				public void run(ProgressIndicator indicator) {
					new PluginDownloader(versionInfo, updateConfiguration).run();
				}
			};

			ProgressManager.getInstance().run(downloader);
		} else if (showConfigPath) {
			Messages.showMessageDialog("You can always install " + aVersion
					+ " version through " + PluginUtil.getInstance().getName()
					+ " configuration panel (Preferences | IDE Settings | "
					+ PluginUtil.getInstance().getName() + " | General | Auto update | Check now)", "Information",
					Messages.getInformationIcon());
		}
		// so or so we mark this version so no more popups will appear
		updateConfiguration.setRejectedUpgrade(versionInfo.getVersion());
	}
}
