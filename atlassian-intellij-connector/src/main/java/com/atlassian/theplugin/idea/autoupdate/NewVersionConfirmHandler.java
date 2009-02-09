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
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author lguminski
 */
public class NewVersionConfirmHandler implements UpdateActionHandler {
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
		NewVersionInfoForm dialog;

		if (project != null) {
			dialog = new NewVersionInfoForm(project, updateConfiguration, versionInfo, showConfigPath);
		} else {
			dialog = new NewVersionInfoForm(parent, updateConfiguration, versionInfo, showConfigPath);
		}
		dialog.show();
	}
}
