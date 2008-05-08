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

import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.ReviewItemVirtualFile;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	private IdeaHelper() {
	}

	@Nullable
	public static Project getCurrentProject() {
		return getCurrentProject(DataManager.getInstance().getDataContext());
	}

	@Nullable
	public static Project getCurrentProject(DataContext dataContext) {
		return DataKeys.PROJECT.getData(dataContext);
	}

	@Nullable
	public static Project getCurrentProject(AnActionEvent e) {
		return getCurrentProject(e.getDataContext());
	}

	@Nullable
	public static JIRAServer getCurrentJIRAServer() {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		if (p == null) {
			return null;
		}
		return p.getComponent(ThePluginProjectComponent.class).getCurrentJiraServer();
	}

	public static List<ReviewItemVirtualFile> getScopeFiles() {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		return p.getComponent(ThePluginProjectComponent.class).getReviewScopeFiles();
	}

	public static ThePluginProjectComponent getCurrentProjectComponent() {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		return p.getComponent(ThePluginProjectComponent.class);
	}

	@Nullable
	public static void setCurrentJIRAServer(JIRAServer jiraServer) {
		Project p = getCurrentProject(DataManager.getInstance().getDataContext());
		if (p == null) {
			return;
		}
		p.getComponent(ThePluginProjectComponent.class).setCurrentJiraServer(jiraServer);
	}

	public static com.intellij.openapi.wm.ToolWindow getToolWindow(Project p) {
		return ToolWindowManager.getInstance(p).getToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);
	}

	public static ThePluginApplicationComponent getAppComponent() {
		return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
	}

	public static PluginConfigurationBean getPluginConfiguration() {
		return getAppComponent().getState();
	}

	public static JIRAToolWindowPanel getCurrentJIRAToolWindowPanel() {
		Project p = getCurrentProject();
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
		return (JIRAToolWindowPanel) content.getComponent();
	}

	public static JIRAToolWindowPanel getJIRAToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}
		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.JIRA.toString());
		return (JIRAToolWindowPanel) content.getComponent();
	}


	public static BambooTableToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event.getDataContext());
		if (p == null) {
			return null;
		}

		ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.BAMBOO.toString());
		return (BambooTableToolWindowPanel) content.getComponent();
	}

	public static ThePluginProjectComponent getCurrentProjectComponent(AnActionEvent e) {
		Project project = getCurrentProject(e.getDataContext());

		if (project == null) {
			return null;
		} else {
			return project.getComponent(ThePluginProjectComponent.class);
		}
	}

	public static CrucibleTableToolWindowPanel getCrucibleToolWindowPanel(AnActionEvent e) {
		Project p = getCurrentProject(e.getDataContext());
		if (p == null) {
			return null;
		}

		ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.CRUCIBLE.toString());

		return (CrucibleTableToolWindowPanel) content.getComponent();
	}
}
