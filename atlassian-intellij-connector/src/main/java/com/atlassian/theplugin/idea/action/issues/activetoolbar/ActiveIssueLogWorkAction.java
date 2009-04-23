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

import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class ActiveIssueLogWorkAction extends AbstractActiveJiraIssueAction {


	public void actionPerformed(final AnActionEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
				final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);

				if (activeIssue != null && panel != null) {
					boolean isOk = false;
					try {
						isOk = panel.logWorkOrDeactivateIssue(ActiveIssueUtils.getJIRAIssue(event),
								IdeaHelper.getCfgManager(event).getServerData(ActiveIssueUtils.getJiraServer(event)),
								StringUtil.generateJiraLogTimeString(activeIssue.recalculateTimeSpent()),
								false);
					} catch (JIRAException e) {
						panel.setStatusMessage("Erroro logging work: " + e.getMessage(), true);
					}

					if (isOk) {
						activeIssue.resetTimeSpent();
					}
				}
			}
		});
	}

	public void onUpdate(final AnActionEvent event) {

	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(enabled);
	}


}
