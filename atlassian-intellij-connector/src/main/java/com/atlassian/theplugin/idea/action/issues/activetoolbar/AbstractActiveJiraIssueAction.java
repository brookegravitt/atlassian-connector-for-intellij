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

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

/**
 * User: pmaruszak
 */
public abstract class AbstractActiveJiraIssueAction extends AnAction {
	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}

	public final void update(final AnActionEvent event) {
		final ActiveJiraIssue activeJiraIssue = ActiveIssueUtils.getActiveJiraIssue(event);
		boolean enabled = activeJiraIssue != null;
		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}


	protected boolean activate(final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
			final JiraServerCfg jiraServerCfg) {
		final Project project = IdeaHelper.getCurrentProject(event);
		boolean isOk = true;
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
		final JiraServerCfg jiraServer = ActiveIssueUtils.getJiraServer(project, newActiveIssue);
		final JIRAIssue jiraIssue = ActiveIssueUtils.getJIRAIssue(jiraServer, newActiveIssue);

		if (panel != null && jiraIssue != null && jiraServer != null) {
			if (jiraServer != null && !jiraServer.getUsername().equals(jiraIssue.getAssigneeId())) {
				isOk = Messages.showYesNoDialog(IdeaHelper.getCurrentProject(event),
						"Is already assigned to " + jiraIssue.getAssignee()
								+ ". Do you want to overwrite assignee and start progress?",
						"Issue " + jiraIssue.getKey(),
						Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
			}

			if (isOk) {
				//assign to me and start working
				isOk = panel.startWorkingOnIssue(jiraIssue, jiraServer);
			}
		}
		return isOk;
	}

	protected boolean deactivate(final AnActionEvent event) {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			ActiveJiraIssueBean activeIssue = conf.getActiveJiraIssue();
			if (activeIssue != null) {
				final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
				final Project project = IdeaHelper.getCurrentProject(event);
				final JIRAIssue jiraIssue = ActiveIssueUtils.getJIRAIssue(project);
				if (panel != null && jiraIssue != null) {
					boolean isOk = true;
					final JiraServerCfg jiraServer = ActiveIssueUtils.getJiraServer(project);


					isOk = panel.logWorkOrDeactivateIssue(jiraIssue,
							jiraServer,
							StringUtil.generateJiraLogTimeString(activeIssue.recalculateTimeSpent()),
							true);


					return isOk;

				}

			}
		}
		return true;
	}

//	protected void refreshLabel(ActiveJiraIssue issue) {
//		ActionManager aManager = ActionManager.getInstance();
//		AnAction action = aManager.getAction(Constants.ACTIVE_JIRA_ISSUE_ACTION);
//		String label = getLabelText(issue);
//		if (issue != null) {
//			action.getTemplatePresentation().setText(label, true);
//			action.getTemplatePresentation().setEnabled(true);
//
//		} else {
//			action.getTemplatePresentation().setText(label, true);
//			action.getTemplatePresentation().setEnabled(false);
//		}
//
//		//createTooltipText("Open Issue", this);
//
//
//	}	
}

