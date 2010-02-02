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
package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks.PluginTaskManager;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateIssueItemAction extends AnAction {
	private final ActiveJiraIssue activeIssue;
	static final Icon JIRA_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	private static final int SUMMARY_LENGHT = 30;

	ActivateIssueItemAction(ActiveJiraIssue activeIssue, final Project project) {
		this.activeIssue = activeIssue;

		RecentlyOpenIssuesCache cache = IdeaHelper.getProjectComponent(project, RecentlyOpenIssuesCache.class);

		String summary = "";
		JiraIssueAdapter issue = null;

		if (cache != null) {
			issue = cache.getLoadedRecenltyOpenIssue(activeIssue.getIssueKey(), activeIssue.getServerId());

			if (issue != null) {
				summary = issue.getSummary();
				if (summary.length() > SUMMARY_LENGHT) {
					summary = summary.substring(0, SUMMARY_LENGHT) + "...";
				}
			}
		}

		if (activeIssue != null) {
			getTemplatePresentation().setText(activeIssue.getIssueKey() + ": " + summary);

			if (issue != null) {
				getTemplatePresentation().setIcon(CachedIconLoader.getIcon(issue.getTypeIconUrl()));
				getTemplatePresentation().setDescription(issue.getSummary());
			}

		}
	}

	public boolean displayTextInToolbar() {
		return true;
	}

	public void actionPerformed(final AnActionEvent event) {
		ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(event);
		if (projectCfgManager != null) {
			final JiraServerData jiraServer = projectCfgManager.getJiraServerr(activeIssue.getServerId());
			if (jiraServer != null) {
				activeIssue.resetTimeSpent();
				if (!PluginTaskManager.isValidIdeaVersion()) {

                      ActiveIssueUtils.activateIssue(IdeaHelper.getCurrentProject(event),
                              event, activeIssue, jiraServer, null);


                } else {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            PluginTaskManager.getInstance(
                                    IdeaHelper.getCurrentProject(event)).activateLocalTask(activeIssue);
                        }
                    });

                }
			}
		}
	}
}
