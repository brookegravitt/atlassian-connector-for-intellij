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
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Simple action to show the settings for the plugin.
 */
public class JIRARefreshIssuesAction extends AnAction {

	public void actionPerformed(AnActionEvent event) {
        IdeaHelper.getJIRAToolWindowPanel(event).refreshIssuesPage();
    }

	public void update(AnActionEvent event) {
		super.update(event);
//		jiraServer = IdeaHelper.getCurrentJIRAServer(event.getDataContext());
//		if (jiraServer != null) {
//			event.getPresentation().setEnabled(jiraServer.isValidServer());
//		} else {
//			event.getPresentation().setEnabled(false);
//		}

		JIRAToolWindowPanel panel = IdeaHelper.getJIRAToolWindowPanel(event);

		if (panel != null) {

			boolean serverSelected = IdeaHelper.getJIRAToolWindowPanel(event).isServerSelected();

			if (serverSelected) {
				JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(event.getDataContext());
				event.getPresentation().setEnabled(jiraServer.isValidServer());
			} else {
				event.getPresentation().setEnabled(false);
			}
		} else {
			event.getPresentation().setEnabled(false);
		}
	}	
}