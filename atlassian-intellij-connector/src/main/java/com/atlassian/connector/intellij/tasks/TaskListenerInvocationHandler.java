/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.connector.intellij.tasks;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks.DeactivateIssueRunnable;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;

import javax.swing.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TaskListenerInvocationHandler implements InvocationHandler {
	private Project project;
	private PluginTaskManager pluginTaskManager;
	private PluginConfiguration pluginConfiguration;

	public TaskListenerInvocationHandler(final Project project, PluginTaskManager pluginTaskManager,
			PluginConfiguration pluginConfiguration) {
		this.project = project;
		this.pluginTaskManager = pluginTaskManager;
		this.pluginConfiguration = pluginConfiguration;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("taskActivated".equals(method.getName())) {
			Object localTask = args[0]; //argument 0 locatTask
			final TaskManagerHelper instance = TaskManagerHelper.getInstance(project);
			String localTaskId = instance.getLocalTaskId(localTask);
			String localTaskUrl = instance.getLocalTaskIssueUrl(localTask);

			if (pluginConfiguration != null
					&& pluginConfiguration.getJIRAConfigurationData().isSynchronizeWithIntelliJTasks()) {
				if (!PluginTaskManager.isDefaultTask(project, localTask)) {
					final ActiveJiraIssue jiraIssue = ActiveIssueUtils.getActiveJiraIssue(project);
					if (jiraIssue == null || !localTaskId.equals(jiraIssue.getIssueKey())) {
						final JiraServerData sd = pluginTaskManager.findJiraPluginJiraServer(localTaskUrl);

						if (sd != null) {
							final ActiveJiraIssueBean ai = new ActiveJiraIssueBean(sd.getServerId(),
									localTaskUrl, localTaskId, new DateTime());

							ai.setSource(ActiveJiraIssueBean.ActivationSource.INTELLIJ);
							ActiveIssueUtils.activateIssue(project, null, ai, sd, null);

						} else {
							//do nothing
						}
					} else {
						//the same or none JIRA issue found inside plugin do nothing
					}

				} else {
					SwingUtilities.invokeLater(new DeactivateIssueRunnable(project));

				}

			}
		} else {
			for (Method m : Object.class.getMethods()) {
				if (m.equals(method)) {
					return m.invoke(this, args);
				}
			}
		}

		return null;
	}

}
