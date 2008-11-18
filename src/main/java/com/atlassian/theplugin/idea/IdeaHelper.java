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

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.Nullable;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	private IdeaHelper() {
	}

	@Nullable
	public static Project getCurrentProject(DataContext dataContext) {
		return DataKeys.PROJECT.getData(dataContext);
	}

	@Nullable
	public static Project getCurrentProject(AnActionEvent e) {
		return getCurrentProject(e.getDataContext());
	}

	public static com.intellij.openapi.wm.ToolWindow getToolWindow(Project p) {
		return ToolWindowManager.getInstance(p).getToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);
	}

	public static ThePluginApplicationComponent getAppComponent() {
		return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
	}

	public static CfgManager getCfgManager() {
		return (CfgManager) ApplicationManager.getApplication().getPicoContainer()
				.getComponentInstanceOfType(CfgManager.class);
	}

	public static PluginConfigurationBean getPluginConfiguration() {
		return getAppComponent().getState();
	}

	public static IssuesToolWindowPanel getIssuesToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event);
		if (p == null) {
			return null;
		}
		return getIssuesToolWindowPanel(p);
	}

	public static IssuesToolWindowPanel getIssuesToolWindowPanel(Project p) {
		if (p == null) {
			return null;
		}

		com.intellij.openapi.wm.ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.ISSUES.toString());
        if (content == null) {
            return null;
        }
        return (IssuesToolWindowPanel) content.getComponent();
	}


	public static BambooTableToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
		Project p = getCurrentProject(event);
		if (p == null) {
			return null;
		}

		ToolWindow tw = getToolWindow(p);
		Content content = tw.getContentManager().findContent(PluginToolWindow.ToolWindowPanels.BAMBOO.toString());
		if (content == null) {
			return null;
		}
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
		if (content == null) {
			return null;
		}
		return (CrucibleTableToolWindowPanel) content.getComponent();
	}

	public static void handleRemoteApiException(final Project project, final RemoteApiException e) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				Messages.showErrorDialog(project, "The following error has occurred while using remote service:\n"
						+ e.getMessage(), "Error while using remote service");
			}
		});
	}

	public static void handleError(final Project project, final ValueNotYetInitialized valueNotYetInitialized) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				PluginUtil.getLogger().warn(valueNotYetInitialized);
				Messages.showErrorDialog(project, "The following error has occurred:\n"
						+ valueNotYetInitialized.getMessage(), "Error");
			}
		});
	}

	/**
	 * Placeholder for handling missing password. Dummy at the moment
	 *
	 * @param e exception to handle
	 * @return true if called should retry the action which caused this excepction, false if it does not make sense
	 *         for example, the user has not provided a new password
	 */
	public static boolean handleMissingPassword(ServerPasswordNotProvidedException e) {
		return false;
	}

	public static <T> T getProjectComponent(final AnActionEvent event, final Class<T> clazz) {
		final Project project = getCurrentProject(event);
		if (project == null) {
			return null;
		}
		return getProjectComponent(project, clazz);
	}

	@Nullable
	public static <T> T getProjectComponent(final Project project, final Class<T> clazz) {
		if (project == null || project.getPicoContainer() == null) {
			return null;
		}
		return clazz.cast(project.getPicoContainer().getComponentInstanceOfType(clazz));
	}
}
