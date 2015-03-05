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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.tasks.PluginTaskManager;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.JIRAFilterListBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Simple helper methods for the IDEA plugin
 */
public final class IdeaHelper {

	private IdeaHelper() {
	}

	public static int getSpinnerIntValue(final JSpinner spinner) {
		if (spinner == null || spinner.getModel() == null) {
			return 1;
		}

		int value = Integer.valueOf(spinner.getModel().getValue().toString());
		try {
			value = NumberFormat.getIntegerInstance().parse(
					((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getText()).intValue();
//			value = Integer.valueOf(((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getText());
		} catch (NumberFormatException e) {
			///not important
		} catch (ParseException e) {
			///not important
		}
		return value;
	}



	@Nullable
	public static Project getCurrentProject(DataContext dataContext) {
		return PlatformDataKeys.PROJECT.getData(dataContext);
	}

	@Nullable
	public static Project getCurrentProject(AnActionEvent e) {
		return e != null ? getCurrentProject(e.getDataContext()) : null;
	}

	public static com.intellij.openapi.wm.ToolWindow getToolWindow(Project p) {
		return ToolWindowManager.getInstance(p).getToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);
	}

    public static PluginConfiguration getPluginConfiguration() {
        return getApplicationComponent(PluginConfiguration.class);
    }
	public static ThePluginApplicationComponent getAppComponent() {
		return ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);
	}

	public static ProjectCfgManager getProjectCfgManager(Project p) {
		return getProjectComponent(p, ProjectCfgManager.class);
	}


	public static ProjectCfgManager getProjectCfgManager(AnActionEvent e) {
		return getProjectComponent(e, ProjectCfgManager.class);
	}

	public static IssueListToolWindowPanel getIssueListToolWindowPanel(AnActionEvent event) {
		return getProjectComponent(event, IssueListToolWindowPanel.class);
	}

	@Nullable
	public static IssueListToolWindowPanel getIssueListToolWindowPanel(@NotNull final Project project) {
		return getProjectComponent(project, IssueListToolWindowPanel.class);
	}

	public static IssueDetailsToolWindow getIssueDetailsToolWindow(AnActionEvent event) {
		return getProjectComponent(event, IssueDetailsToolWindow.class);
	}

	public static IssueDetailsToolWindow getIssueDetailsToolWindow(@NotNull final Project project) {
		return getProjectComponent(project, IssueDetailsToolWindow.class);
	}

	@Nullable
	public static BambooToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
		return getProjectComponent(event, BambooToolWindowPanel.class);
	}

	@Nullable
	public static BambooToolWindowPanel getBambooToolWindowPanel(@NotNull final Project project) {
		return getProjectComponent(project, BambooToolWindowPanel.class);
	}

	public static BuildToolWindow getBuildToolWindow(AnActionEvent event) {
		return getProjectComponent(event, BuildToolWindow.class);
	}

	public static BuildToolWindow getBuildToolWindow(@NotNull final Project project) {
		return getProjectComponent(project, BuildToolWindow.class);
	}

	public static JiraWorkspaceConfiguration getJiraWorkspaceConfiguration(AnActionEvent e) {
		return getProjectComponent(e, JiraWorkspaceConfiguration.class);

	}

    public static JiraWorkspaceConfiguration getJiraWorkspaceConfiguration(Project project) {
		return getProjectComponent(project, JiraWorkspaceConfiguration.class);

	}

	public static ThePluginProjectComponent getCurrentProjectComponent(AnActionEvent e) {
		Project project = getCurrentProject(e.getDataContext());

		return getCurrentProjectComponent(project);
	}

	public static ThePluginProjectComponent getCurrentProjectComponent(Project project) {
		if (project == null || project.isDisposed()) {
			return null;
		} else {
			return project.getComponent(ThePluginProjectComponent.class);
		}
	}

	public static void handleRemoteApiException(final Project project, final RemoteApiException e) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				DialogWithDetails.showExceptionDialog(project, "The following error has occurred while using remote service:\n"
						+ e.getMessage(), DialogWithDetails.getExceptionString(e)
						+ (e.getServerStackTrace() != null ? e.getServerStackTrace() : ""));
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
		return getProjectComponent(getCurrentProject(event), clazz);
	}

	@Nullable
	public static <T> T getProjectComponent(final Project project, final Class<T> clazz) {
		if (project == null || project.isDisposed()) {
			return null;
		}
		return clazz.cast(project.getPicoContainer().getComponentInstanceOfType(clazz));
	}

	@Nullable
	public static <T> T getApplicationComponent(final Class<T> clazz) {
		return clazz.cast(ApplicationManager.getApplication().getPicoContainer().getComponentInstanceOfType(clazz));
	}

	public static JIRAIssueListModelBuilder getJIRAIssueListModelBuilder(final AnActionEvent event) {
		final Project project = getCurrentProject(event);
		return getJIRAIssueListModelBuilder(project);
	}

	public static JIRAIssueListModelBuilder getJIRAIssueListModelBuilder(final Project project) {
//		final ThePluginProjectComponent pluginProjectComponent = getCurrentProjectComponent(project);
//		if (pluginProjectComponent != null) {
//			return pluginProjectComponent.getJiraIssueListModelBuilder();
//		}
		return getProjectComponent(project, JIRAIssueListModelBuilder.class);
	}

	public static JIRAFilterListBuilder getJIRAFilterListBuilder(final Project project) {
		return getProjectComponent(project, JIRAFilterListBuilder.class);
	}

	public static JIRAFilterListBuilder getJIRAFilterListBuilder(final AnActionEvent event) {
//		final ThePluginProjectComponent pluginProjectComponent = getCurrentProjectComponent(event);
//		if (pluginProjectComponent != null) {
//			return pluginProjectComponent.getJiraFilterListBuilder();
//		}

		return getProjectComponent(event, JIRAFilterListBuilder.class);
	}

	public static JIRAServerModel getJIRAServerModel(final AnActionEvent event) {
//		final ThePluginProjectComponent pluginProjectComponent = getCurrentProjectComponent(event);
//		if (pluginProjectComponent != null) {
//			return pluginProjectComponent.getJiraServerModel();
//		}
		return getProjectComponent(event, JIRAServerModel.class);
	}

	public static JIRAServerModel getJIRAServerModel(final Project project) {
//		final ThePluginProjectComponent pluginProjectComponent = getCurrentProjectComponent(project);
//		if (pluginProjectComponent != null) {
//			return pluginProjectComponent.getJiraServerModel();
//		}
//
//		return null;
		return getProjectComponent(project, JIRAServerModel.class);
	}

	public static PluginTaskManager getPluginTaskManager(final Project project) {
		return getProjectComponent(project, PluginTaskManager.class);
	}
}
