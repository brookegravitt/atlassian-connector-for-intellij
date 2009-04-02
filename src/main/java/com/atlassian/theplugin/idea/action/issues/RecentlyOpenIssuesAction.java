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

import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

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

		final IssuesToolWindowPanel issuesWindow = IdeaHelper.getIssuesToolWindowPanel(e);
		if (issuesWindow == null) {
			return;
		}

		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(e, JiraWorkspaceConfiguration.class);
		if (conf == null) {
			return;
		}

		final List<IssueRecentlyOpenBean> recentlyOpenIssues = conf.getRecentlyOpenIssues();

		if (recentlyOpenIssues.size() > 0) {
			// prepare list of recentlyOpenIssues from the config list

			ProgressManager.getInstance().run(new Task.Modal(project, "Retrieving Recently Open Issues", true) {
				private List<JIRAIssue> issues;

				public void run(final ProgressIndicator indicator) {
					issues = issuesWindow.getIssues(recentlyOpenIssues);
				}

				public void onSuccess() {
					ListPopup popup =
							JBPopupFactory.getInstance().createListPopup(
									new IssueListPopupStep("Recently Open Issues", issues, issuesWindow));
//					popup.showCenteredInCurrentWindow(project); that can cause NPE inside IDEA OpenAPI
					popup.showInCenterOf(e.getInputEvent().getComponent());
				}
			});

		} else {
			Messages.showInfoMessage(project, "No recently open issues found.", PluginUtil.PRODUCT_NAME);
		}
	}

	public static final class IssueListPopupStep extends BaseListPopupStep<JIRAIssue> {
		private IssuesToolWindowPanel issuesWindow;
		private static final int LENGHT = 40;

		public IssueListPopupStep(final String title, final List<JIRAIssue> reviews,
				final IssuesToolWindowPanel issuesWindow) {
			super(title, reviews, IconLoader.getIcon("/icons/jira-blue-16.png"));
			this.issuesWindow = issuesWindow;
		}

		@NotNull
		@Override
		public String getTextFor(final JIRAIssue value) {
			StringBuilder text = new StringBuilder();

			text.append(value.getKey()).append(": ");

			if (value.getSummary().length() > LENGHT) {
				text.append(value.getSummary().substring(0, LENGHT - (2 + 1))).append("...");
			} else {
				text.append(value.getSummary());
			}

			text.append(" (");

			if (value.getServer().getName().length() > LENGHT) {
				text.append(value.getServer().getName().substring(0, LENGHT - (2 + 1)));
			} else {
				text.append(value.getServer().getName());
			}

			text.append(')');

			return text.toString();
		}

		@Override
		public PopupStep onChosen(final JIRAIssue selectedValue, final boolean finalChoice) {
			// add review to the model (to show it in the main list) and open the review
			issuesWindow.openIssue(selectedValue, selectedValue.getServer());

			return null;
		}
	}
}
