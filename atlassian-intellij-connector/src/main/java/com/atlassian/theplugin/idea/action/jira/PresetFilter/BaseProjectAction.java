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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @autrhor pmaruszak
 * @date Mar 25, 2010
 */
public abstract class BaseProjectAction extends AnAction {
    protected Project project;
    protected IssueListToolWindowPanel panel;
    protected JiraServerData selectedServer;
    protected JiraPresetFilter selectedPresetFilter;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = IdeaHelper.getCurrentProject(event);
        panel = IdeaHelper.getIssueListToolWindowPanel(project);
        selectedServer = panel.getSelectedServer();
        selectedPresetFilter = panel.getSelectedPresetFilter();
        onActionPerformed(event);
    }

    protected abstract void onActionPerformed(AnActionEvent event);
}
