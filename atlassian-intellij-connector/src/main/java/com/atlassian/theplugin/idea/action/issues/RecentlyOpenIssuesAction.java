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

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class RecentlyOpenIssuesAction extends AnAction {

	@Override
	public void actionPerformed(final AnActionEvent e) {

		final Project project = IdeaHelper.getCurrentProject(e);
		if (project == null) {
			return;
		}

		final IssueListToolWindowPanel issuesWindow = IdeaHelper.getIssueListToolWindowPanel(e);
		if (issuesWindow == null) {
			return;
		}

		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(e, JiraWorkspaceConfiguration.class);
		if (conf == null) {
			return;
		}

		final List<IssueRecentlyOpenBean> recentlyOpenIssues = conf.getRecentlyOpenIssuess();

		if (recentlyOpenIssues.size() > 0) {
			// prepare list of recentlyOpenIssues from the config list

			List<JiraIssueAdapter> issues = issuesWindow.getLoadedRecenltyOpenIssues();

			ListPopup popup = JBPopupFactory.getInstance().createListPopup(
					new IssueListPopupStep("Recently Viewed Issues", issues, issuesWindow));
//					popup.showCenteredInCurrentWindow(project); that can cause NPE inside IDEA OpenAPI
			popup.showInCenterOf(e.getInputEvent().getComponent());
		} else {
			Messages.showInfoMessage(project, "No recently viewed issues found.", PluginUtil.PRODUCT_NAME);
		}
	}

	public static final class IssueListPopupStep extends BaseListPopupStep<JiraIssueAdapter> {
		private IssueListToolWindowPanel issuesWindow;
		private static final int LENGHT = 40;

		public IssueListPopupStep(final String title, final List<JiraIssueAdapter> issues,
				final IssueListToolWindowPanel issuesWindow) {
			super(title, issues);
			this.issuesWindow = issuesWindow;
		}

		public Icon getIconFor(final JiraIssueAdapter issue) {
			return CachedIconLoader.getIcon(issue.getTypeIconUrl());
		}

		@NotNull
		@Override
		public String getTextFor(final JiraIssueAdapter value) {

			if (value == null) {
				return "null";
			}

			StringBuilder text = new StringBuilder();

			text.append(value.getKey()).append(": ");

			if (value.getSummary().length() > LENGHT) {
				text.append(value.getSummary().substring(0, LENGHT - (2 + 1))).append("...");
			} else {
				text.append(value.getSummary());
			}

			text.append(" (");

			if (value.getJiraServerData().getName().length() > LENGHT) {
				text.append(value.getJiraServerData().getName().substring(0, LENGHT - (2 + 1)));
			} else {
				text.append(value.getJiraServerData().getName());
			}

			text.append(')');

			return text.toString();
		}

		@Override
		public PopupStep onChosen(final JiraIssueAdapter selectedValue, final boolean finalChoice) {
			// add review to the model (to show it in the main list) and open the review
			issuesWindow.openIssue(selectedValue, true);

			return null;
		}
	}
}
