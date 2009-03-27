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

import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueLabel extends AbstractActiveJiraIssueAction {
	public boolean displayTextInToolbar() {
		return true;
	}

	public void actionPerformed(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JIRAIssue issue = getJIRAIssue(event);
				if (issue != null) {
					IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
					if (panel != null) {
						panel.openIssue(issue);
					}
				}
			}
		});

	}

	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		Presentation presentation = event.getPresentation();
		final Project project = IdeaHelper.getCurrentProject(event);
		if (enabled && project != null) {
			ActiveJiraIssue issue = getActiveJiraIssue(event);
			issue.recalculateTimeSpent();
			String jiraTimeSpent = StringUtil.generateJiraLogTimeString(issue.getSecondsSpent());
			if (jiraTimeSpent.length() > 0) {
				jiraTimeSpent += ":" + jiraTimeSpent;
			}

			presentation.setText("Active Issue:"
					+ issue.getIssueKey()
					+ jiraTimeSpent);
			presentation.setEnabled(true);
		}

	}
}
