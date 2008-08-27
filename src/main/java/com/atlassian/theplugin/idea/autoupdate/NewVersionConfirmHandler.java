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
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.commons.util.Version;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 13, 2008
 * Time: 1:27:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewVersionConfirmHandler implements UpdateActionHandler {
	private static final String DOWNLOAD_TITLE = "Downloading new " + PluginUtil.getInstance().getName() + " plugin version ";

	@Nullable
	private final Project project;
	private GeneralConfigurationBean updateConfiguration;
	private Component parent;

	public NewVersionConfirmHandler(@Nullable Component parent, @Nullable Project project,
			GeneralConfigurationBean updateConfiguration) {
		this.project = project;
		this.updateConfiguration = updateConfiguration;
		this.parent = parent;

		if (parent == null && project == null) {
			throw new IllegalArgumentException("You must specify at least one not null value for parent or project");
		}
	}

	public void doAction(final InfoServer.VersionInfo versionInfo, boolean showConfigPath) throws ThePluginException {
		NewVersionDialogInfo dialog = null;

		if (project != null) {
			dialog = new NewVersionDialogInfo(project, updateConfiguration, versionInfo);
		} else {
			dialog = new NewVersionDialogInfo(parent, updateConfiguration, versionInfo);
		}
		dialog.show();
	}
}
