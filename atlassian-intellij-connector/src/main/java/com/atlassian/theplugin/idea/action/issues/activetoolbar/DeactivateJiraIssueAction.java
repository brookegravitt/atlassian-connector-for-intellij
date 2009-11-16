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

import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.ActiveIssueResultHandler;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class DeactivateJiraIssueAction extends AbstractActiveJiraIssueAction {
    public void actionPerformed(final AnActionEvent event) {
        runDeactivateTask(event);
    }

    public static void runDeactivateTask(final AnActionEvent event) {


        SwingUtilities.invokeLater(new LocalRunnable(event));

//                //success is invoked only if actions are selected (ie. stop progress action)
//                if (isOk && conf != null) {
//                    conf.setActiveJiraIssuee(null);
//                    PluginTaskManager.getInstance(IdeaHelper.getCurrentProject(event)).deactivateToDefaultTask();
//                }
    }

    private static class LocalRunnable implements Runnable {
        private final Project project;
        private final JiraWorkspaceConfiguration conf;
        private final AnActionEvent event;

        public LocalRunnable(AnActionEvent event) {

            this.event = event;
            conf = IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);
            project = IdeaHelper.getCurrentProject(event);
        }


        public void run() {
            ActiveIssueUtils.deactivate(event, new ActiveIssueResultHandler() {
                public void success() {
                    if (conf != null) {
                        conf.setActiveJiraIssuee(null);
                        PluginTaskManager.getInstance(IdeaHelper.getCurrentProject(event)).deactivateToDefaultTask();
                    }
                }

                public void failure(final Throwable problem) {
                    if (conf != null) {
                        conf.setActiveJiraIssuee(null);
                        PluginTaskManager.getInstance(IdeaHelper.getCurrentProject(event)).deactivateToDefaultTask();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (project != null && !project.isDisposed()) {
                                DialogWithDetails.showExceptionDialog(
                                        project,
                                        "Issue Deactivated Locally but Failed to Deactivate Issue remotely.", problem);
                            }
                        }
                    });
                }

                public void cancel(String problem) {
                    //deactivation cancelled
                    PluginTaskManager.getInstance(project).addChangeListListener();
                }
            });
        }
    }

    public void onUpdate(final AnActionEvent event) {
    }

    public void onUpdate(final AnActionEvent event, final boolean enabled) {
        event.getPresentation().setEnabled(enabled);
    }
}
