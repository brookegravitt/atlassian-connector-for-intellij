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
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.AbstractActiveJiraIssueAction;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssue extends AbstractActiveJiraIssueAction {
	public void actionPerformed(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {


			public void run() {
				final JIRAIssue selectedIssue = getSelectedJiraIssue(event);
				if (selectedIssue != null) {
					final Project project = IdeaHelper.getCurrentProject(event);
					JiraServerCfg jiraServerCfg = getSelectedJiraServer(event);
					final ActiveJiraIssue activeIssue =
							new ActiveJiraIssueImpl(project, jiraServerCfg, selectedIssue, new DateTime());
					setActiveJiraIssue(event, activeIssue);
				}
			}
		});


	}

	public void onUpdate(final AnActionEvent event) {
	}
}
