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

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssueBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.joda.time.DateTime;

/**
 * User: pmaruszak
 */
public class ActivateJiraIssueAction extends AbstractActiveJiraIssueAction {

	public void actionPerformed(final AnActionEvent event) {
		JiraIssueAdapter selectedIssue;
		selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);

		if (selectedIssue != null) {
			if (!isSelectedIssueActive(event, selectedIssue)) {
				if (selectedIssue.getJiraServerData() != null) {
					ActiveJiraIssue newActiveIssue = new ActiveJiraIssueBean(
							selectedIssue.getJiraServerData().getServerId(), selectedIssue.getKey(), new DateTime());
                    Project project = IdeaHelper.getCurrentProject(event);
					ActiveIssueUtils.activateIssue(project, event, newActiveIssue, selectedIssue.getJiraServerData());
				}
			} else {
				DeactivateJiraIssuePopupAction.runDeactivateTask(event);
			}
		}
	}

	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		final JiraIssueAdapter selectedIssue = ActiveIssueUtils.getSelectedJiraIssue(event);

		if (isSelectedIssueActive(event, selectedIssue)) {
			event.getPresentation().setEnabled(true);
			event.getPresentation().setText("Stop Work");
            event.getPresentation().setIcon(IconLoader.getIcon("/icons/ico_inactiveissue.png"));
		} else if (selectedIssue != null) {
			event.getPresentation().setEnabled(true);
			event.getPresentation().setText("Start Work");
            event.getPresentation().setIcon(IconLoader.getIcon("/icons/ico_activateissue.png"));
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}