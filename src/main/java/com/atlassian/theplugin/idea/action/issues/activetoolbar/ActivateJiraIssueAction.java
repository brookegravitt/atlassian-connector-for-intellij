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
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.joda.time.DateTime;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssueAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(final AnActionEvent event) {
		JIRAIssue selectedIssue = null;
		JiraServerCfg jiraServerCfg = null;
		selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);

		if (selectedIssue != null) {
			jiraServerCfg = ActiveIssueUtils.getSelectedJiraServerByUrl(event, selectedIssue.getServerUrl());
			ActiveJiraIssue newActiveIssue;
			if (jiraServerCfg != null) {
				newActiveIssue =
						new ActiveJiraIssueBean(jiraServerCfg.getServerId().toString(), selectedIssue.getKey(),
								new DateTime());
				activateIssue(event, newActiveIssue, jiraServerCfg);
			}
		}
	}

	public void activateIssue(final AnActionEvent event, final ActiveJiraIssue newActiveIssue,
			final JiraServerCfg jiraServerCfg) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
				boolean isAlreadyActive = activeIssue != null;
				boolean isDeactivated = true;
				if (isAlreadyActive) {

					isDeactivated = Messages.showYesNoDialog(IdeaHelper.getCurrentProject(event),
							activeIssue.getIssueKey()
									+ " is active. Would you like to deactivate it first and proceed?",
							"Deactivating current issue",
							Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE;
				}
				if (isDeactivated && deactivate(event)) {
					final boolean isActivated = activate(event, newActiveIssue, jiraServerCfg);
					if (isActivated) {
						ActiveIssueUtils.setActiveJiraIssue(event, newActiveIssue, jiraServerCfg);
					} else {
						ActiveIssueUtils.setActiveJiraIssue(event, null, jiraServerCfg);
					}
				}
			}
		});
	}


	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		final JIRAIssue selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);
		final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);

		if (selectedIssue != null && activeIssue != null
				&& ActiveIssueUtils.getSelectedJiraServerById(event, activeIssue.getServerId()) != null) {

			final JiraServerCfg selectedServer = ActiveIssueUtils.getSelectedJiraServerById(event, activeIssue.getServerId());
			final boolean equals = selectedIssue.getKey().equals(activeIssue.getIssueKey())
					&& selectedServer.getServerId().toString().equals(activeIssue.getServerId());
			event.getPresentation().setEnabled(!equals);
		} else if (selectedIssue != null) {
			event.getPresentation().setEnabled(true);
		} else {
			event.getPresentation().setEnabled(false);
		}
	}

}