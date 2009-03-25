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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public abstract class AbstractActiveJiraIssueAction extends AnAction {
	public void actionPerformed(final AnActionEvent event) {

	}

	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}

	public final void update(final AnActionEvent event) {
		boolean enabled = getActiveJiraIssue(event) != null ? true : false;

		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}

	protected ActiveJiraIssue getActiveJiraIssue(final AnActionEvent event) {
		final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);

		if (panel != null) {
			final JIRAIssueListModel model = panel.getBaseIssueListModel();
			if (model != null) {
				return model.getActiveJiraIssue();
			}
		}

		return null;
	}

	protected JIRAIssue getJiraIssue(final AnActionEvent event) {
		final ActiveJiraIssue activeIssue = getActiveJiraIssue(event);
		if (activeIssue != null) {
			return activeIssue.getIssue();
		}

		return null;
	}
}
