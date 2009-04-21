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

import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueComboBox extends ComboBoxAction {
	static final Icon JIRA_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	static final Icon JIRA_ICON_DISABLED = IconLoader.getIcon("/icons/jira-grey-16.png");

	public void update(final AnActionEvent event) {
		ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
		String text = "No active issue";
		if (activeIssue != null) {
			text = activeIssue.getIssueKey();
		}

		event.getPresentation().setText(text);
//		event.getPresentation().setIcon(JIRA_ICON);
		super.update(event);
	}

	@NotNull
	protected DefaultActionGroup createPopupActionGroup(final JComponent jComponent) {
		final Project currentProject = IdeaHelper
				.getCurrentProject(DataManager.getInstance().getDataContext(jComponent));


		DefaultActionGroup group = new DefaultActionGroup("Issues to activate", true);
		final JiraWorkspaceConfiguration conf = IdeaHelper
				.getProjectComponent(currentProject, JiraWorkspaceConfiguration.class);
		final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(currentProject);
		if (conf != null) {
			for (IssueRecentlyOpenBean issue : conf.getRecentlyOpenIssues()) {
				if (activeIssue == null || !issue.getIssueKey().equals(activeIssue.getIssueKey())) {
					ActiveJiraIssue newActiveIsse = new ActiveJiraIssueBean(issue.getServerId(), issue.getIssueKey(),
							new DateTime());
					group.add(new ActivateIssueItemAction(newActiveIsse));
				}
			}
		}

		return group;
	}
}
