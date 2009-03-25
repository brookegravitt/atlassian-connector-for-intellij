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
package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueImpl;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssue extends AnAction {
	public void actionPerformed(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
				if (panel != null) {
					JIRAIssueListModel model = panel.getBaseIssueListModel();
					if (model != null) {
						Project project = IdeaHelper.getCurrentProject(event);
						JiraServerCfg jiraCfg = panel.getSelectedServer();
						JIRAIssueBean selectedIssue = (JIRAIssueBean) event.getData(Constants.ISSUE_KEY);
						ActiveJiraIssue activeIssue = new ActiveJiraIssueImpl(project, jiraCfg, selectedIssue);
						activeIssue.activate();
						model.setActiveJiraIssue(activeIssue);
					}
				}
			}
		});


	}
}
