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

package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.cfg.CfgUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public class SelectJIRAAction extends ComboBoxAction {

	@NotNull
	@Override
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {

		final Project project = IdeaHelper.getCurrentProject(jComponent);

		final DefaultActionGroup g = new DefaultActionGroup();


		Collection<JiraServerCfg> servers = IdeaHelper.getCfgManager().getAllEnabledJiraServers(
				CfgUtil.getProjectId(project));

		final ComboBoxButton button = (ComboBoxButton) jComponent;
		for (final JiraServerCfg server : servers) {
			g.add(new AnAction(server.getName()) {
				@Override
				public void actionPerformed(AnActionEvent event) {
					button.setText(event.getPresentation().getText());
					IdeaHelper.getJIRAToolWindowPanel(event).selectServer(server);
				}
			});
		}
		return g;
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getCurrentJIRAServer() != null) {
			event.getPresentation().setText(IdeaHelper.getCurrentJIRAServer().getServer().getName());
		}
	}
}
