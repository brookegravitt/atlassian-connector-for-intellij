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
package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.bamboo.AbstractBuildAction;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * @author Jacek Jaroczynski
 *
 * Used in the panel with build details
 */
public abstract class AbstractBuildDetailsAction extends AbstractBuildAction {

	@Override
	protected BambooBuildAdapter getBuild(final AnActionEvent e) {
		BuildToolWindow btw = IdeaHelper.getBuildToolWindow(e);
		if (btw != null) {
			return btw.getBuild(e.getPlace());
		}

		return null;
	}


	@Override
	protected List<BambooBuildAdapter> getBuilds(final AnActionEvent e) {
		BambooToolWindowPanel btw = IdeaHelper.getBambooToolWindowPanel(e);
		if (btw != null) {
			return btw.getSelectedBuilds();
		}

		return null;
	}
	@Override
	protected void setStatusMessage(final Project project, final String message) {
		// no status bar in the build details panel at the moment
	}

	protected void setStatusErrorMessage(final Project project, final String message) {
		// no status bar in the build details panel at the moment
	}

	@Override
	public void update(final AnActionEvent event) {
		// enabled all the time
	}
}
