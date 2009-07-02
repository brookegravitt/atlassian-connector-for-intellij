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

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public class DeactivateJiraIssuePopupAction extends DeactivateJiraIssueAction {

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		final JIRAIssue selectedJiraIssue = ActiveIssueUtils.getSelectedJiraIssue(event);
		final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
		ServerData selectedServer;
		ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(event);

		if (activeIssue != null && activeIssue.getIssueKey() != null && projectCfgManager != null) {
			selectedServer = projectCfgManager.getJiraServerr(activeIssue.getServerId());
			event.getPresentation().setEnabled(enabled && selectedJiraIssue != null
					&& selectedJiraIssue.getKey().equals(activeIssue.getIssueKey())
					&& selectedServer != null && selectedServer.getServerId().equals(activeIssue.getServerId()));
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}
