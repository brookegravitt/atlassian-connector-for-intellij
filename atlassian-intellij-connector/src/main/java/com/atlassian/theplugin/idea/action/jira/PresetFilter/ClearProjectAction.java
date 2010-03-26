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
package com.atlassian.theplugin.idea.action.jira.PresetFilter;

import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @autrhor pmaruszak
 * @date Mar 24, 2010
 */
public class ClearProjectAction extends BaseProjectAction {
    @Override
    protected void onActionPerformed(AnActionEvent anActionEvent) {
        final Project project = IdeaHelper.getCurrentProject(anActionEvent);
        final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
        final JiraServerData selectedServer = panel.getSelectedServer();

        final JiraPresetFilter selectedPresetFilter = panel.getSelectedPresetFilter();
        IdeaHelper.getJiraWorkspaceConfiguration(anActionEvent)
                    .clearPresetFilterProject(selectedServer, selectedPresetFilter);
         //selectedPresetFilter.setJiraProject(null);
         panel.refreshIssues(true);
    }
}
