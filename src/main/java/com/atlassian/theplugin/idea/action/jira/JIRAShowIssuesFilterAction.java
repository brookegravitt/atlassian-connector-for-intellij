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

package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;

@Deprecated
public class JIRAShowIssuesFilterAction extends AnAction {
	private static final String DEFAULT_ICON = "/ant/filter.png";

	public JIRAShowIssuesFilterAction() {
		getTemplatePresentation().setIcon(IconLoader.getIcon(DEFAULT_ICON));
		getTemplatePresentation().setText("Show JIRA Filter Dialog");
	}

	public void actionPerformed(AnActionEvent e) {
		
		final JIRAToolWindowPanel jiraPanel = IdeaHelper.getJIRAToolWindowPanel(e);

		if (jiraPanel != null) {
			jiraPanel.showJIRAIssueFilter();
		}
	}

	public void update(AnActionEvent event) {
		super.update(event);

		JIRAToolWindowPanel toolWindowPanel = IdeaHelper.getJIRAToolWindowPanel(event);

		if (toolWindowPanel != null) {
			if (toolWindowPanel.getFilters().getSavedFilterUsed()
					&& !event.getPlace().equals(toolWindowPanel.getActionPlace())) {
				event.getPresentation().setEnabled(false);
			} else {
				event.getPresentation().setEnabled(true);
			}
		} else {
			event.getPresentation().setEnabled(false);
		}

	}
}

