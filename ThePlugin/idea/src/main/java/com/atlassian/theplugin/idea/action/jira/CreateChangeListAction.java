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
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;

@Deprecated
public class CreateChangeListAction extends AnAction {

    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = DataKeys.PROJECT.getData(anActionEvent.getDataContext());
        IdeaHelper.getJIRAToolWindowPanel(anActionEvent).createChangeListAction(project);
    }

    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);

        JIRAIssue issue = IdeaHelper.getJIRAToolWindowPanel(anActionEvent).getSelectedIssue();
        if (issue != null) {
            String changeListName = issue.getKey() + " - " + issue.getSummary();

            Project project = DataKeys.PROJECT.getData(anActionEvent.getDataContext());
            if (ChangeListManager.getInstance(project).findChangeList(changeListName) == null) {
                anActionEvent.getPresentation().setText("Create ChangeList");

            } else {
                anActionEvent.getPresentation().setText("Activate ChangeList");
            }
        }
    }
}
