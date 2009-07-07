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

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.joda.time.DateTime;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssueAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(final AnActionEvent event) {
		JIRAIssue selectedIssue;
		selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);

		if (selectedIssue != null) {
			if (!isSelectedIssueActive(event, selectedIssue)) {
				if (selectedIssue.getServer() != null) {
					ActiveJiraIssue newActiveIssue = new ActiveJiraIssueBean(
							selectedIssue.getServer().getServerId(), selectedIssue.getKey(), new DateTime());

					ActiveIssueUtils.activateIssue(event, newActiveIssue, selectedIssue.getServer());
				}
			} else {
				DeactivateJiraIssuePopupAction.runDeactivateTask(event);
			}
		}
	}

	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		final JIRAIssue selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);

		if (isSelectedIssueActive(event, selectedIssue)) {
			event.getPresentation().setEnabled(true);
			event.getPresentation().setText("Stop Work");
		} else if (selectedIssue != null) {
			event.getPresentation().setEnabled(true);
			event.getPresentation().setText("Start Work");
		} else {
			event.getPresentation().setEnabled(false);
		}
	}

	private static boolean isSelectedIssueActive(final AnActionEvent event, JIRAIssue selectedIssue) {
		final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);

		ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(event);

		if (selectedIssue != null && activeIssue != null && projectCfgManager != null
				&& projectCfgManager.getJiraServerr(activeIssue.getServerId()) != null) {

			final ServerData selectedServer = projectCfgManager.getJiraServerr(activeIssue.getServerId());

			return selectedIssue.getKey().equals(activeIssue.getIssueKey())
					&& selectedServer.getServerId().equals(activeIssue.getServerId());
		}
		return false;
	}
}