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
package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author Jacek Jaroczynski
 *         <p/>
 *         Used in the panel with builds list
 */
public abstract class AbstractBuildListAction extends AbstractBuildAction {

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		BambooBuildAdapter build = getBuild(event);
		event.getPresentation().setEnabled(build != null
				&& build.isEnabled() && build.getPlanKey() != null && build.isValid());
	}

	@Override
	protected BambooBuildAdapter getBuild(final AnActionEvent event) {
		BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(event, BambooToolWindowPanel.class);
		if (panel != null) {
			return panel.getSelectedBuild();
		}
		return null;
	}

	@Override
	protected void setStatusMessage(final Project project, final String message) {
		BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(project, BambooToolWindowPanel.class);
		if (panel != null) {
			panel.setStatusMessage(message);
		}
	}

	@Override
	protected void setStatusErrorMessage(final Project project, final String message) {
		BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(project, BambooToolWindowPanel.class);
		if (panel != null) {
			panel.setErrorMessage(message);
		}
	}

	protected boolean isBamboo2(final AnActionEvent event, final BambooServerData serverData) {

		return serverData != null && serverData.isBamboo2();

	}
}

