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
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssueAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				final JIRAIssue selectedIssue = getSelectedJiraIssue(event);
				if (selectedIssue != null) {
					JiraServerCfg jiraServerCfg = getSelectedJiraServer(event);
					final ActiveJiraIssue activeIssue =
							new ActiveJiraIssueBean(jiraServerCfg.getServerId().toString(), selectedIssue.getKey(),
									new DateTime());


					if (activate(event, activeIssue)) {
						setActiveJiraIssue(event, activeIssue);
						registerToolbar(event);
					}
				}
			}
		});


	}

	private boolean activate(final AnActionEvent event, final ActiveJiraIssue activeIssue) {
		final Project project = IdeaHelper.getCurrentProject(event);
		boolean isOk = false;
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(project);
		final JiraServerCfg jiraServer = getJiraServer(event, activeIssue);
		final JIRAIssue issue = getJIRAIssue(jiraServer, activeIssue);
		if (panel != null && issue != null && jiraServer != null) {
			//assign to me and start working
			isOk = panel.startWorkingOnIssue(issue, jiraServer);
		}

		return isOk;
	}


	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(!enabled);
	}

	private void registerToolbar(final AnActionEvent event) {

//		ActionManager aManager = ActionManager.getInstance();
//			ActionGroup menu = (ActionGroup) aManager.getAction(Constants.ACTIVE_TOOLBAR_NAME);
//			DefaultActionGroup mainToolBar = (DefaultActionGroup) aManager.getAction("MainToolBar");
//
//
//			if (menu != null && mainToolBar != null) {
//				//aManager.createActionToolbar("ActionToolbar", menu, true);
//				mainToolBar.add(new ActiveJiraIssueLabel(), Constraints.LAST, aManager);
//				mainToolBar.add(new DeactivateJiraIssueAction(), Constraints.LAST, aManager);
//				mainToolBar.add(new ActiveIssueLogWorkAction(), Constraints.LAST, aManager);
//				mainToolBar.add(new ActiveIssueCommentAction(), Constraints.LAST, aManager);
//			}

	}
}
