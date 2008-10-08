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

import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;

public class QuickSearchAction extends AnAction {
    @Override
	public void actionPerformed(AnActionEvent e) {
        Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(project);

        if (jiraServer != null) {
            String query = Messages.showInputDialog(project, "Quick Search:",
                    "Search", IconLoader.getIcon("/actions/find.png"));
            if (query != null) {
                BrowserUtil.launchBrowser(jiraServer.getServer().getUrl()
                        + "/secure/QuickSearch.jspa?searchString=" + UrlUtil.encodeUrl(query));
            }
        } else {
            PluginToolWindow.focusPanel(e, PluginToolWindow.ToolWindowPanels.JIRA);
            Messages.showErrorDialog(project, "Please select a JIRA server before searching.", "JIRA Quick Search");
        }
    }

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		JIRAServer jiraServer = IdeaHelper.getCurrentJIRAServer(event.getDataContext());
		if (jiraServer != null) {
			event.getPresentation().setEnabled(jiraServer.isValidServer());
		} else {
			event.getPresentation().setEnabled(false);
		}
	}	
}
