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

package com.atlassian.theplugin.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.ServerType;

import javax.swing.*;

public abstract class StatusBarPluginIcon extends JLabel {
	private StatusBar statusBar = null;
	private Project project;

	private boolean isIconShown = false;

	public StatusBarPluginIcon(Project aProject) {
		this.project = aProject;
	}

	public void hideIcon() {
		statusBar = WindowManager.getInstance().getStatusBar(project);
		if (statusBar == null) {
			return;
		}
		if (isIconShown) {
			statusBar.removeCustomIndicationComponent(this);
			WindowManager.getInstance().getFrame(project).repaint();
			isIconShown = false;
		}
	}

	public void showIcon() {
		statusBar = WindowManager.getInstance().getStatusBar(project);
		if (statusBar == null) {
			return;
		}
		if (!isIconShown) {
			statusBar.addCustomIndicationComponent(this);
			WindowManager.getInstance().getFrame(project).repaint();
			isIconShown = true;
		}
	}

	/**
	 * Shows or hides icon for specified product (Bamboo/Crucible) depending if there are defined servers or not
	 * @param serverType type of Icon to show/hide
	 */
	protected void showOrHideIcon(ServerType serverType) {
		if (ConfigurationFactory.getConfiguration().getProductServers(serverType).getServers().size() > 0) {
			showIcon();
		} else {
			hideIcon();
		}
	}

	public boolean isIconShown() {
		return isIconShown;
	}
}
