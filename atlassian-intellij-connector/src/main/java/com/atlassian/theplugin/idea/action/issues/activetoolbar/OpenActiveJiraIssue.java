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

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class OpenActiveJiraIssue extends AnAction {
	public void actionPerformed(final AnActionEvent event) {
		openIssue(event);
	}

	public void update(final AnActionEvent event) {
		event.getPresentation().setEnabled(ActiveIssueUtils.getActiveJiraIssue(event) != null);
	}

	private void openIssue(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final Project currentProject = IdeaHelper
						.getCurrentProject(event);
				final JiraIssueAdapter issue;
				if (currentProject != null) {
					final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(currentProject);
					try {
						issue = ActiveIssueUtils.getJIRAIssue(currentProject);
						if (issue != null) {
							if (panel != null) {
								panel.openIssue(issue, true);
							}
						}
					} catch (JIRAException e) {
						if (panel != null) {
							panel.setStatusErrorMessage("Error opening issue: " + e.getMessage(), e);
						}
					}
				}
			}
		});
	}
}
