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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.bamboo.BuildToolWindow;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsToolWindow;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.JIRAFilterListBuilder;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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

//	@Nullable
//	public static CfgManager getCfgManager(final AnActionEvent event) {
//		ThePluginProjectComponent ppc = getCurrentProjectComponent(event);
//		if (ppc != null) {
//			return ppc.getCfgManager();
//		}
//
//		return null;
//	}
//
//	@Nullable
//	public static CfgManager getCfgManager(final Project project) {
//		ThePluginProjectComponent ppc = getCurrentProjectComponent(project);
//		if (ppc != null) {
//			return ppc.getCfgManager();
//		}
//
//		return null;
//	}

	public static ProjectCfgManagerImpl getProjectCfgManager(Project p) {
		return getProjectComponent(p, ProjectCfgManagerImpl.class);
	}


	public static ProjectCfgManagerImpl getProjectCfgManager(AnActionEvent e) {
		return getProjectComponent(e, ProjectCfgManagerImpl.class);
	}

	public static IssueListToolWindowPanel getIssueListToolWindowPanel(AnActionEvent event) {
		return getProjectComponent(event, IssueListToolWindowPanel.class);
	}

	public static IssueListToolWindowPanel getIssueListToolWindowPanel(@NotNull final Project project) {
		return getProjectComponent(project, IssueListToolWindowPanel.class);
	}

	public static IssueDetailsToolWindow getIssueDetailsToolWindow(AnActionEvent event) {
		return getProjectComponent(event, IssueDetailsToolWindow.class);
	}

	public static IssueDetailsToolWindow getIssueDetailsToolWindow(@NotNull final Project project) {
		return getProjectComponent(project, IssueDetailsToolWindow.class);
	}

	public static ReviewDetailsToolWindow getReviewDetailsToolWindow(@NotNull final Project project) {
		return getProjectComponent(project, ReviewDetailsToolWindow.class);
	}

	@Nullable
	public static ReviewDetailsToolWindow getReviewDetailsToolWindow(@NotNull final AnActionEvent event) {
		return getProjectComponent(getCurrentProject(event), ReviewDetailsToolWindow.class);
	}

	@Nullable
	public static ReviewListToolWindowPanel getReviewListToolWindowPanel(AnActionEvent event) {
		return getProjectComponent(event, ReviewListToolWindowPanel.class);
	}

	public static ReviewListToolWindowPanel getReviewListToolWindowPanel(@NotNull final Project project) {
		return getProjectComponent(project, ReviewListToolWindowPanel.class);
	}

    @Nullable
    public static BambooToolWindowPanel getBambooToolWindowPanel(AnActionEvent event) {
        return getProjectComponent(event, BambooToolWindowPanel.class);
    }

	public static BuildToolWindow getBuildToolWindow(AnActionEvent event) {
		return getProjectComponent(event, BuildToolWindow.class);
	}

	public static BuildToolWindow getBuildToolWindow(@NotNull final Project project) {
		return getProjectComponent(project, BuildToolWindow.class);
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
		return getProjectComponent(getCurrentProject(event), clazz);
	}

	@Nullable
	public static <T> T getProjectComponent(final Project project, final Class<T> clazz) {
		if (project == null || project.isDisposed()) {
			return null;
		}
		return clazz.cast(project.getPicoContainer().getComponentInstanceOfType(clazz));
	}

	public static CrucibleStatusChecker getCrucibleStatusChecker(Project project) {

		return getCurrentProjectComponent(project).getCrucibleStatusChecker();
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

}
