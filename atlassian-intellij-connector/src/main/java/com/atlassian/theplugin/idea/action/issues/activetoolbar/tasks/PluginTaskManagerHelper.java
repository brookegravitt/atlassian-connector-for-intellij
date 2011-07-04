package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.connector.intellij.tasks.PluginTaskManager;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * @author pmaruszak
 * @date Feb 2, 2010
 */
public final class PluginTaskManagerHelper {
	private PluginTaskManagerHelper() {

	}

	public static void activateIssue(final Project project, final ActiveJiraIssue issue) {

		if (!isValidIdeaVersion()) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					final PluginTaskManager ptm = IdeaHelper.getPluginTaskManager(project);
					ptm.activateIssue(issue);
				} catch (Exception e) {
					PluginUtil.getLogger().error("Cannot activate issue.", e);
				}
			}
		});
	}

	public static boolean isValidIdeaVersion() {
		return !IdeaVersionFacade.getInstance().isIdea7()
				&& !IdeaVersionFacade.getInstance().isIdea8()
				&& !IdeaVersionFacade.getInstance().isCommunityEdition()
				&& isTaskPluginEnabled()
				&& IdeaHelper.getPluginConfiguration().getJIRAConfigurationData().isSynchronizeWithIntelliJTasks();
	}

	private static boolean isTaskPluginEnabled() {
		IdeaPluginDescriptor descriptor = getTaskManagerDescriptor();
		return descriptor != null && ((IdeaPluginDescriptorImpl) descriptor).isEnabled();
	}

	public static IdeaPluginDescriptor getTaskManagerDescriptor() {
		for (IdeaPluginDescriptor descriptor : ApplicationManager.getApplication().getPlugins()) {
			if (descriptor.getPluginId().getIdString().equals("com.intellij.tasks")) {
				return descriptor;
			}
		}
		return null;
	}

	public static void deactivateToDefaultTask(final Project project) {
		if (!isValidIdeaVersion()) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					final PluginTaskManager ptm = IdeaHelper.getPluginTaskManager(project);
					if (!ptm.isDefaultTaskActive()) {
						ptm.deactivateToDefaultTask();
					} else {
						SwingUtilities.invokeLater(new DeactivateIssueRunnable(project));
					}

				} catch (Exception e) {
					PluginUtil.getLogger().error("Cannot deactivate issue to default task.", e);
				}
			}
		});
	}
}
