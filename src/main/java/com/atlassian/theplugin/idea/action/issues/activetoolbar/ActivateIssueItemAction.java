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
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActivateIssueItemAction extends AnAction {
	private final ActiveJiraIssue activeIssue;
	static final Icon JIRA_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");

	ActivateIssueItemAction(ActiveJiraIssue activeIssue) {
		this.activeIssue = activeIssue;
		if (activeIssue != null) {
			getTemplatePresentation().setText(activeIssue.getIssueKey());
			getTemplatePresentation().setIcon(JIRA_ICON);
		}
	}

	public boolean displayTextInToolbar() {
		return true;
	}

	public void actionPerformed(final AnActionEvent event) {
		JiraServerCfg jiraServer = ActiveIssueUtils.getSelectedJiraServerById(event, activeIssue.getServerId());
		if (activeIssue != null && jiraServer != null) {
			activeIssue.resetTimeSpent();
			ActiveIssueUtils.activateIssue(event, activeIssue, jiraServer);
		}
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		if (activeIssue != null) {
			event.getPresentation().setText(activeIssue.getIssueKey());
		} else {
			event.getPresentation().setEnabled(false);
			event.getPresentation().setText("unknown");
		}


	}
}
